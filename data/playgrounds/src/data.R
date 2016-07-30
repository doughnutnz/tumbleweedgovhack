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


## Clean up data ######################

d.park_clean <- d.park %>% 
  group_by(SITEDESCRI) %>% 
  summarise(
    equipment = sprintf("<ul>%s</ul>", paste0("<li>", DESCRIPTIO, "</li>", collapse = " ")),
    address = sprintf(
      "%s %s, %s, %s", 
      first(STREETNUMB), 
      first(STREETNAME),
      first(LOCALBOARD),
      first(CITY)),
    long = median(coords.x1),
    lat = median(coords.x2)
  ) %>% 
  rename(name = SITEDESCRI) %>% 
  mutate(
    # id = seq(1, n()),
    id = first(site),
    facilities = "playground",
    url = "http://google.com",
    geocode_address = url,
    about = "Local Park - Leisure",
    long = as.character(long),
    lat = as.character(lat)
  )


## Draw map of equipment ##############

leaflet(d.park_clean) %>% 
  addProviderTiles("CartoDB.Positron") %>% 
  addMarkers(
    lng = ~long,
    lat = ~lat,
    popup = ~paste0("<strong>", name, "</strong><br>", address, "<br><br>Equipment:", equipment))


## Export to JSON #####################

sink(file = "data/parks_geocoded.json")
toJSON(d.park_clean, pretty = T)
sink()


write.csv(d.park, file = "data/parks.csv", row.names = F)
