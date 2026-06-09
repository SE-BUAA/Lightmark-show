UPDATE product
SET extra = JSON_SET(
        COALESCE(extra, JSON_OBJECT()),
        '$.destination', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.destination')), name),
        '$.depart_city', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.depart_city')), '北京'),
        '$.date', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.date')), '2026-07-15'),
        '$.days', COALESCE(JSON_EXTRACT(extra, '$.days'), 5),
        '$.hotel_level', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.hotel_level')), '四星'),
        '$.summary', COALESCE(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.summary')), '精选度假产品')
    ),
    category_tags = COALESCE(category_tags, JSON_ARRAY('度假', '精选')),
    status = COALESCE(status, 1)
WHERE product_type = 'VACATION';
