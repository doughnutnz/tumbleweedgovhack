# Define here the models for your scraped items
#
# See documentation in:
# http://doc.scrapy.org/topics/items.html

from scrapy.item import Item, Field


class Park(Item):
    name = Field()
    address = Field()
    facilities = Field()
    about = Field()
    url = Field()
    # sports = Field()
