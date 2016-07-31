#!/bin/bash

rm parks.csv
scrapy crawl park_crawl -o parks.csv
