# Scrapy settings for webcrawler project
#
# For simplicity, this file contains only the most important settings by
# default. All the other settings are documented here:
#
#     http://doc.scrapy.org/topics/settings.html
#

# name of the crawler
BOT_NAME = 'webcrawler'

# spiders to use in the crawling
SPIDER_MODULES = ['webcrawler.spiders']
NEWSPIDER_MODULE = 'webcrawler.spiders'


# Crawl responsibly by identifying yourself (and your website) on the user-agent
USER_AGENT = 'Scrapy/0.24 (+http://scrapy.org)'


# The amount of time (in secs) that the downloader should wait before downloading
# consecutive pages from the same website. This can be used to throttle the crawling
# speed to avoid hitting servers too hard. Decimal numbers are supported.
DOWNLOAD_DELAY = 0


# If enabled, Scrapy will respect robots.txt policies
ROBOTSTXT_OBEY = False


# The maximum depth that will be allowed to crawl for any site. If zero, no limit will be imposed.
DEPTH_LIMIT = 1
