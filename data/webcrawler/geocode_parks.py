#!/usr/bin/env python

import sys
import re
import csv
from geopy.geocoders import GoogleV3
from Levenshtein import distance, ratio


def clean_string(s):
    return ''.join([i for i in s if ord(i) < 128])


def clean_address(address, park_name):
    # remove street number letters
    address = address.lower()
    nums = re.findall(ur'\d+[a-z]+', address)
    for num in nums:
        n = re.sub(ur'[a-z]+', '', num)
        address = address.replace(num, n)

    # extract suburb
    parts = address.split(',')
    suburb = parts[-1]
    address = ', '.join(parts[:-1])

    # abbreviate "Road", "Avenue" etc
    abbreviations = [
        ('street', 'st'),
        ('road', 'rd'),
        ('avenue', 'ave'),
        ('terrace', 'ter'),
        ('crescent', 'cres'),
        ('drive', 'dr'),
        ('lane', 'ln'),
        ('boulevard', 'blvd')]
    for pattern, replacement in abbreviations:
        address = address.replace(pattern, replacement)

    # check for street number range pattern
    p = ur'\d+ to \d+'
    m = re.findall(p, address)
    if len(m) > 0:
        # only take the first street number
        street_number = m[0].split(' to ')[0]
        address = re.sub(p, street_number, address)

    # check for "between" pattern
    p = ur'between \d+ and \d+'
    m = re.findall(p, address)
    if len(m) > 0:
        # only use the first street number
        street_number = m[0].replace('between ', '').split(' and ')[0]
        address = re.sub(p, street_number, address)

    # check for "opposite" pattern
    p = ur'opposite \d+'
    m = re.findall(p, address)
    if len(m) > 0:
        # only use the first street number
        address = address.replace('opposite ', '')

    # use park name as address if street number is missing
    p = ur'^\d+'
    m = re.findall(p, address)
    if len(m) == 0:
        # use park name as address
        address_parts = [park_name] + address.split(',')[1:]
        address = ', '.join(address_parts)

    return address, suburb


def geocode(geolocator, address, timeout=5):
    # try geocoding 3 times in case some attempts fail
    location = None
    for i in range(3):
        try:
            location = geolocator.geocode(address, timeout=timeout)
            break
        except:
            print('Exception geocoding record:\n%s' % address)
            continue

    return location


if __name__ == '__main__':
    # setup the geolocator backend
    geolocator = GoogleV3()

    # open parks CSV and geocode line by line
    with open('parks.csv') as f:
        csv_reader = csv.reader(f, delimiter=',')

        # setup output csv
        fout = open('parks_geocoded.csv', 'w')
        writer = csv.writer(fout)

        address_index = None
        name_index = None
        for i, row in enumerate(csv_reader):
            # on the firt row just print the CSV header
            if i == 0:
                # find the address column
                try:
                    address_index = row.index('address')
                    name_index = row.index('name')
                except ValueError:
                    print('Could not find address or name column: %s\nTerminating.' % str(row))
                    sys.exit(1)
                writer.writerow(row + ['geocode_address_suburb', 'long_suburb', 'lat_suburb',
                                       'geocode_address_no_suburb', 'long_no_suburb', 'lat_no_suburb',
                                       'dist_suburb', 'dist_no_suburb', 'ratio_suburb', 'ratio_no_suburb'])
                continue

            # extract address
            address, suburb = clean_address(row[address_index], row[name_index])

            # try with suburb info first
            address_suburb = ', '.join([address, suburb, 'Auckland, New Zealand'])
            location_suburb = geocode(geolocator, address_suburb, timeout=5)

            # try without suburb info
            address_no_suburb = ', '.join([address, 'Auckland, New Zealand'])
            location_no_suburb = geocode(geolocator, address_no_suburb, timeout=5)

            # compute difference between geocoded and original address
            try:
                ratio_suburb = ratio(address_suburb.lower(), str(location_suburb[0].lower()))
                dist_suburb = distance(address_suburb.lower(), str(location_suburb[0].lower()))
            except:
                ratio_suburb = -1.
                dist_suburb = -1.
            try:
                ratio_no_suburb = ratio(address_no_suburb.lower(), str(location_no_suburb[0].lower()))
                dist_no_suburb = distance(address_no_suburb.lower(), str(location_no_suburb[0].lower()))
            except:
                ratio_no_suburb = -1.
                dist_no_suburb = -1.

            # combine results into a new CSV row
            if location_suburb or location_no_suburb:
                r = [location_suburb[0], str(location_suburb[1][0]), str(location_suburb[1][1]),
                     location_no_suburb[0], str(location_no_suburb[1][0]), str(location_no_suburb[1][1]),
                     dist_suburb, dist_no_suburb, ratio_suburb, ratio_no_suburb]
                record = row + r
            else:
                record = row + (['NA'] * 10)

            writer.writerow(record)
            try:
                print('%d\t%s\t%s\t%s\t%f\t%d\t%f\t%d' % (i, 'OK' if location_suburb or location_no_suburb else 'NA',
                row[address_index], location_suburb[0] if ratio_suburb > ratio_no_suburb else location_no_suburb[0],
                ratio_suburb, dist_suburb, ratio_no_suburb, dist_no_suburb))
            except:
                print('%d could not print' % i)

        f.close()
