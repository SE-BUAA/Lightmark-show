INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, extra, status)
SELECT 'VACATION', '三亚海岛五日自由行', 2999.00, 30, 0,
       JSON_ARRAY('海岛', '自由行', '亲子'),
       JSON_OBJECT('destination', '三亚', 'depart_city', '北京', 'date', '2026-06-15', 'days', 5, 'hotel_level', '五星', 'summary', '海景酒店连住，含接送机')
       , 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_type = 'VACATION' AND name = '三亚海岛五日自由行');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, extra, status)
SELECT 'VACATION', '云南古城六日跟团游', 2580.00, 24, 0,
       JSON_ARRAY('古城', '跟团游', '摄影'),
       JSON_OBJECT('destination', '丽江', 'depart_city', '上海', 'date', '2026-06-20', 'days', 6, 'hotel_level', '四星', 'summary', '丽江大理双城，含经典景点门票')
       , 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_type = 'VACATION' AND name = '云南古城六日跟团游');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, extra, status)
SELECT 'VACATION', '成都美食四日私享团', 1880.00, 18, 0,
       JSON_ARRAY('美食', '私享团', '城市'),
       JSON_OBJECT('destination', '成都', 'depart_city', '广州', 'date', '2026-07-02', 'days', 4, 'hotel_level', '四星', 'summary', '小团出行，城市美食和周边慢游')
       , 1
WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_type = 'VACATION' AND name = '成都美食四日私享团');
