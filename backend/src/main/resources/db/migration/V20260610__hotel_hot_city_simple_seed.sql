-- 简洁版酒店演示数据：8 个热门城市，每城 20 家酒店，共 160 家。
-- 可重复执行：同名酒店和同房型不会重复插入。

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE room_type ADD COLUMN bed_type VARCHAR(50) DEFAULT NULL COMMENT ''床型''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'room_type'
    AND COLUMN_NAME = 'bed_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE room_type ADD COLUMN area VARCHAR(50) DEFAULT NULL COMMENT ''房间面积''',
    'SELECT 1'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'room_type'
    AND COLUMN_NAME = 'area'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO product (product_type, name, price, stock, sold_count, status, extra, category_tags, create_time, update_time)
SELECT 'HOTEL',
       CONCAT(a.city, a.landmark, s.suffix),
       a.base_price + s.price_delta,
       60,
       s.sold_count,
       1,
       JSON_OBJECT(
         'address', CONCAT(a.landmark, '附近', s.address_no, '号'),
         'city', a.city,
         'landmark', a.landmark,
         'starLevel', s.star_level,
         'brand', s.brand,
         'coverImage', s.cover_image,
         'lat', a.lat,
         'lng', a.lng,
         'facilities', JSON_ARRAY('早餐', '停车场', '免费WiFi', s.facility),
         'seedBatch', 'HOTEL_SIMPLE_20260610'
       ),
       JSON_ARRAY(a.city, a.landmark, s.brand, CONCAT(s.star_level, '星')),
       NOW(),
       NOW()
FROM (
  SELECT '北京' city, '王府井' landmark, 39.9155 lat, 116.4110 lng, 520 base_price UNION ALL
  SELECT '北京', '国贸', 39.9087, 116.4600, 560 UNION ALL
  SELECT '北京', '三里屯', 39.9348, 116.4551, 540 UNION ALL
  SELECT '北京', '中关村', 39.9847, 116.3167, 450 UNION ALL
  SELECT '北京', '北京南站', 39.8652, 116.3785, 390 UNION ALL
  SELECT '上海', '外滩', 31.2397, 121.4903, 620 UNION ALL
  SELECT '上海', '陆家嘴', 31.2356, 121.5065, 650 UNION ALL
  SELECT '上海', '南京西路', 31.2305, 121.4598, 560 UNION ALL
  SELECT '上海', '迪士尼', 31.1429, 121.6671, 520 UNION ALL
  SELECT '上海', '虹桥枢纽', 31.1962, 121.3207, 460 UNION ALL
  SELECT '广州', '珠江新城', 23.1200, 113.3236, 520 UNION ALL
  SELECT '广州', '北京路', 23.1258, 113.2688, 410 UNION ALL
  SELECT '广州', '琶洲会展', 23.0989, 113.3667, 480 UNION ALL
  SELECT '广州', '长隆', 23.0058, 113.3270, 500 UNION ALL
  SELECT '广州', '广州南站', 22.9908, 113.2694, 390 UNION ALL
  SELECT '深圳', '福田中心', 22.5431, 114.0579, 540 UNION ALL
  SELECT '深圳', '南山科技园', 22.5329, 113.9474, 520 UNION ALL
  SELECT '深圳', '华侨城', 22.5390, 113.9870, 500 UNION ALL
  SELECT '深圳', '深圳湾', 22.4996, 113.9410, 580 UNION ALL
  SELECT '深圳', '宝安机场', 22.6393, 113.8107, 420 UNION ALL
  SELECT '成都', '太古里', 30.6570, 104.0808, 450 UNION ALL
  SELECT '成都', '春熙路', 30.6598, 104.0804, 430 UNION ALL
  SELECT '成都', '宽窄巷子', 30.6697, 104.0608, 420 UNION ALL
  SELECT '成都', '熊猫基地', 30.7355, 104.1457, 360 UNION ALL
  SELECT '成都', '成都东站', 30.6291, 104.1418, 330 UNION ALL
  SELECT '杭州', '西湖', 30.2576, 120.1432, 520 UNION ALL
  SELECT '杭州', '钱江新城', 30.2462, 120.2120, 500 UNION ALL
  SELECT '杭州', '武林广场', 30.2741, 120.1634, 430 UNION ALL
  SELECT '杭州', '灵隐寺', 30.2407, 120.1020, 480 UNION ALL
  SELECT '杭州', '杭州东站', 30.2919, 120.2120, 360 UNION ALL
  SELECT '西安', '钟楼', 34.2610, 108.9422, 380 UNION ALL
  SELECT '西安', '大唐不夜城', 34.2145, 108.9640, 430 UNION ALL
  SELECT '西安', '大雁塔', 34.2180, 108.9642, 410 UNION ALL
  SELECT '西安', '回民街', 34.2648, 108.9380, 360 UNION ALL
  SELECT '西安', '西安北站', 34.3765, 108.9402, 320 UNION ALL
  SELECT '三亚', '海棠湾', 18.3102, 109.7411, 680 UNION ALL
  SELECT '三亚', '亚龙湾', 18.2330, 109.6415, 650 UNION ALL
  SELECT '三亚', '大东海', 18.2207, 109.5230, 520 UNION ALL
  SELECT '三亚', '三亚湾', 18.2831, 109.4520, 480 UNION ALL
  SELECT '三亚', '凤凰机场', 18.3029, 109.4123, 380
) a
JOIN (
  SELECT '臻选酒店' suffix, 'Lightmark臻选' brand, 5 star_level, 180 price_delta, 120 sold_count, '行政酒廊' facility, 18 address_no,
         'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80' cover_image UNION ALL
  SELECT '云际酒店', '云际', 5, 120, 96, '泳池', 36,
         'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '雅居酒店', '雅居', 4, 40, 76, '洗衣房', 58,
         'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '轻居酒店', '轻居', 3, -80, 54, '近地铁', 88,
         'https://images.unsplash.com/photo-1559599189-fe84dea4eb79?auto=format&fit=crop&w=1200&q=80'
) s
WHERE NOT EXISTS (
  SELECT 1
  FROM product p
  WHERE p.product_type = 'HOTEL'
    AND p.name = CONCAT(a.city, a.landmark, s.suffix)
);

INSERT INTO room_type (hotel_id, room_name, price, cancel_policy, breakfast, bed_type, area)
SELECT p.id,
       r.room_name,
       p.price + r.price_delta,
       r.cancel_policy,
       r.breakfast,
       r.bed_type,
       r.area
FROM product p
JOIN (
  SELECT '舒适大床房' room_name, 0 price_delta, 'FREE_CANCEL' cancel_policy, 1 breakfast, '大床' bed_type, '30㎡' area UNION ALL
  SELECT '商务双床房', 90, 'FREE_CANCEL', 1, '双床', '36㎡'
) r
WHERE p.product_type = 'HOTEL'
  AND JSON_UNQUOTE(JSON_EXTRACT(p.extra, '$.seedBatch')) = 'HOTEL_SIMPLE_20260610'
  AND NOT EXISTS (
    SELECT 1
    FROM room_type rt
    WHERE rt.hotel_id = p.id
      AND rt.room_name = r.room_name
  );
