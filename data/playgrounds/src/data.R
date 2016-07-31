#################################################
# Load GDB database and plot playgrounds on a map
# 
# Author: Stefan Schliebs
# Created: 29 Jul 2016
#################################################


library(plyr)
library(dplyr)
library(leaflet)
library(rgdal)
library(rgeos)
library(jsonlite)


## Load shape file ####################

ogrListLayers("/Users/stefanschliebs/Downloads/Playgrounds.gdb")

sp.park <- readOGR("/Users/stefanschliebs/Downloads/Playgrounds.gdb", "PlaygroundEquipment")

d.park <- spTransform(sp.park, CRS("+proj=longlat")) %>% 
  as.data.frame() %>% 
  tbl_df()


## Load park equipment ################

# NOTE: We export d.park and manually clean the playground equipment items
write.csv(d.park, file = "data/parks.csv", row.names = F)

# load the cleaned file back into R
d.equipment <- read.csv("data/park_equipment.csv", stringsAsFactors = T) %>% tbl_df()


## Load webscraped parks ##############

d.scraped <- read.csv("data/parks_geocoded.csv", stringsAsFactors = F) %>% 
  tbl_df()


## Clean up data ######################

d.park_clean <- d.park %>% 
  mutate(id = SITE %>% as.character() %>% as.integer()) %>% 
  left_join(
    d.equipment %>% 
      group_by(SITE) %>% 
      summarise(
        equipment = paste0(Category, collapse = "<br>"),
        # equipment = sprintf("<ul>%s</ul>", paste0("<li>", Category, "</li>", collapse = " ")),
        nb_items = n_distinct(Category)
        # nb_items = Category %>% unique() %>% length()
      ), 
    by = c("id" = "SITE")) %>% 
  group_by(SITEDESCRI) %>% 
  summarise(
    long = median(coords.x1),
    lat = median(coords.x2),
    address = sprintf("%s %s, %s", first(STREETNUMB), first(STREETNAME), first(CITY)),
    nb_items = first(nb_items),
    equipment = first(equipment),
    id = first(id)
    # equipment = sprintf("<ul>%s</ul>", paste0("<li>", DESCRIPTIO, "</li>", collapse = " ")),
    # nb_items = Category %>% unique() %>% length()
  ) %>% 
  rename(name = SITEDESCRI) %>% 
  mutate(
    long = as.character(long),
    lat = as.character(lat),
    name = as.character(name)
  ) %>% 
  left_join(
    d.scraped %>% select(url, facilities, about, name), 
    by = "name") %>% 
  mutate(
    facilities = ifelse(is.na(facilities), "", facilities),
    about = ifelse(is.na(about), "", about),
    url = ifelse(is.na(url), "", url)
  )



## Draw map of equipment ##############

leaflet(d.park_clean) %>% 
  addProviderTiles("CartoDB.Positron") %>% 
  setView(lng = median(d.park_clean$long), lat = median(d.park_clean$lat), zoom = 10) %>% 
  addMarkers(
    lng = ~long,
    lat = ~lat,
    popup = ~paste0("<strong>", name, "</strong><br>", address, "<br><br>Equipment:", equipment))


## Export to JSON #####################

sink(file = "data/parks_geocoded.json")
toJSON(d.park_clean, pretty = T)
sink()

