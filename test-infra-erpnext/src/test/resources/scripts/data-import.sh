#!/bin/bash

bench --site frontend data-import --file /data/units_of_measure/insert_UOM.csv --doctype 'UOM' --type 'Insert' --submit-after-import --mute-emails;
bench --site frontend data-import --file /data/item_group/insert_item_group.csv --doctype 'Item Group' --type 'Insert' --submit-after-import --mute-emails;
bench --site frontend data-import --file /data/items/insert_item.csv --doctype 'Item' --type 'Insert' --submit-after-import --mute-emails;
bench --site frontend data-import --file /data/item_price/insert_item_price.csv --doctype 'Item Price' --type 'Insert' --submit-after-import --mute-emails;
