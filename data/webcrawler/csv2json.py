#!/usr/bin/env python

import csv
import json


with open('parks_geocoded_manual2.csv') as f:
    csv_reader = csv.reader(f, delimiter='\t')

    records = []
    for i, row in enumerate(csv_reader):
        if i == 0:
            fields = row
        else:
            record = {'id': i}
            for j, f in enumerate(fields):
                record[f] = row[j]
            records.append(record)

    print(json.dumps(records, indent=4))
