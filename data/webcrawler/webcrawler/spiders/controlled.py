from scrapy.contrib.spiders import CrawlSpider, Rule
from scrapy.contrib.linkextractors import LinkExtractor

from ..items import Park


class CollectorSpider(CrawlSpider):
    name = 'park_crawl'
    allowed_domains = ['nz']

    start_urls = ['http://www.aucklandcity.govt.nz/whatson/places/parksonline/default2.asp?'
                  'pName=&pAddress=&pSuburb=&pSports1=&pSports2=&pFacility1=Playground&'
                  'pFacility2=&pFacility3=&status=go&Search=Go']

    rules = (
        # Parse links with the spider's method parse_item
        Rule(LinkExtractor(allow=('detail2.asp')), callback='parse_item', follow=True),
    )

    def parse_item(self, response):
        p = Park()
        p['name'] = ''.join(response.xpath(".//*[@id='newparks']/div/h2/text()").extract()).strip()
        p['address'] = ''.join(response.xpath(".//*[@id='newparks']/div/p[1]/text()").extract()).strip()
        p['url'] = response.request.url

        headline_2 = ''.join(response.xpath(".//*[@id='newparks']/div/h4[2]/text()").extract()).strip().lower()
        par_2 = response.xpath(".//*[@id='newparks']/div/p[2]/text()").extract()

        if headline_2.startswith('about the'):
            p['about'] = ''.join(par_2).strip()
        elif headline_2.startswith('facilities'):
            p['facilities'] = ''.join(par_2).strip()
        else:
            print('------> UNKNOWN H.2:', headline_2)

        headline_3 = ''.join(response.xpath(".//*[@id='newparks']/div/h4[3]/text()").extract()).strip().lower()
        par_3 = response.xpath(".//*[@id='newparks']/div/p[3]/text()").extract()

        if headline_3.startswith('about the'):
            p['about'] = ''.join(par_3).strip()
        elif headline_3.startswith('facilities'):
            p['facilities'] = ''.join(par_3).strip()
        else:
            print('------> UNKNOWN H.3:', headline_3)

        # some cleaning up
        if 'about' in p and p['about'].endswith(','):
            p['about'] = p['about'][:-1]

        if 'facilities' in p and p['facilities'].endswith(','):
            p['facilities'] = p['facilities'][:-1]

        return p
