-- 补充版酒店演示数据：8 个热门城市，每城新增 30 家酒店，共 240 家。
-- 可重复执行：同名酒店和同房型不会重复插入。

INSERT INTO product (product_type, name, price, stock, sold_count, status, extra, category_tags, create_time, update_time)
SELECT 'HOTEL',
       CONCAT(a.city, a.landmark, s.suffix),
       a.base_price + s.price_delta,
       60,
       s.sold_count,
       1,
       JSON_OBJECT(
         'address', CONCAT(a.landmark, '商圈', s.address_no, '号'),
         'city', a.city,
         'landmark', a.landmark,
         'starLevel', s.star_level,
         'brand', s.brand,
         'coverImage', s.cover_image,
         'lat', a.lat,
         'lng', a.lng,
         'facilities', JSON_ARRAY('早餐', '停车场', '免费WiFi', s.facility),
         'seedBatch', 'HOTEL_SIMPLE_EXTRA_20260611'
       ),
       JSON_ARRAY(a.city, a.landmark, s.brand, CONCAT(s.star_level, '星')),
       NOW(),
       NOW()
FROM (
  SELECT '北京' city, '后海' landmark, 39.9446 lat, 116.3862 lng, 480 base_price UNION ALL
  SELECT '北京', '前门', 39.8995, 116.3974, 430 UNION ALL
  SELECT '北京', '望京', 39.9968, 116.4698, 420 UNION ALL
  SELECT '北京', '奥体中心', 39.9917, 116.3965, 470 UNION ALL
  SELECT '北京', '西单', 39.9134, 116.3741, 460 UNION ALL
  SELECT '北京', '798艺术区', 39.9840, 116.4950, 440 UNION ALL
  SELECT '上海', '新天地', 31.2196, 121.4752, 540 UNION ALL
  SELECT '上海', '静安寺', 31.2230, 121.4465, 560 UNION ALL
  SELECT '上海', '徐家汇', 31.1836, 121.4368, 480 UNION ALL
  SELECT '上海', '人民广场', 31.2304, 121.4737, 520 UNION ALL
  SELECT '上海', '世博园', 31.1850, 121.4890, 500 UNION ALL
  SELECT '上海', '豫园', 31.2270, 121.4920, 510 UNION ALL
  SELECT '广州', '上下九', 23.1138, 113.2454, 360 UNION ALL
  SELECT '广州', '天河体育中心', 23.1340, 113.3215, 460 UNION ALL
  SELECT '广州', '白云山', 23.1774, 113.2973, 380 UNION ALL
  SELECT '广州', '沙面', 23.1068, 113.2395, 420 UNION ALL
  SELECT '广州', '大学城', 23.0439, 113.3861, 330 UNION ALL
  SELECT '广州', '越秀公园', 23.1402, 113.2668, 390 UNION ALL
  SELECT '深圳', '罗湖口岸', 22.5324, 114.1178, 430 UNION ALL
  SELECT '深圳', '大梅沙', 22.5968, 114.3068, 520 UNION ALL
  SELECT '深圳', '会展中心', 22.5367, 114.0587, 540 UNION ALL
  SELECT '深圳', '前海', 22.5320, 113.8945, 500 UNION ALL
  SELECT '深圳', '龙岗中心城', 22.7209, 114.2469, 340 UNION ALL
  SELECT '深圳', '东门老街', 22.5455, 114.1188, 390 UNION ALL
  SELECT '成都', '锦里', 30.6426, 104.0494, 390 UNION ALL
  SELECT '成都', '天府广场', 30.6574, 104.0657, 410 UNION ALL
  SELECT '成都', '高新金融城', 30.5770, 104.0646, 460 UNION ALL
  SELECT '成都', '环球中心', 30.5688, 104.0667, 430 UNION ALL
  SELECT '成都', '东郊记忆', 30.6740, 104.1190, 360 UNION ALL
  SELECT '成都', '武侯祠', 30.6424, 104.0487, 400 UNION ALL
  SELECT '杭州', '西溪湿地', 30.2705, 120.0599, 450 UNION ALL
  SELECT '杭州', '湖滨银泰', 30.2558, 120.1659, 460 UNION ALL
  SELECT '杭州', '运河拱宸桥', 30.3211, 120.1426, 350 UNION ALL
  SELECT '杭州', '滨江互联网', 30.1880, 120.2110, 420 UNION ALL
  SELECT '杭州', '宋城', 30.1719, 120.1014, 410 UNION ALL
  SELECT '杭州', '萧山机场', 30.2295, 120.4344, 360 UNION ALL
  SELECT '西安', '曲江池', 34.2002, 108.9753, 390 UNION ALL
  SELECT '西安', '高新科技路', 34.2308, 108.8930, 360 UNION ALL
  SELECT '西安', '城墙南门', 34.2474, 108.9427, 370 UNION ALL
  SELECT '西安', '兵马俑', 34.3840, 109.2780, 350 UNION ALL
  SELECT '西安', '小寨', 34.2295, 108.9453, 350 UNION ALL
  SELECT '西安', '永宁门', 34.2470, 108.9423, 380 UNION ALL
  SELECT '三亚', '市中心', 18.2528, 109.5119, 360 UNION ALL
  SELECT '三亚', '蜈支洲岛码头', 18.3147, 109.7578, 560 UNION ALL
  SELECT '三亚', '鹿回头', 18.2189, 109.5105, 450 UNION ALL
  SELECT '三亚', '崖州湾', 18.3587, 109.1720, 340 UNION ALL
  SELECT '三亚', '免税城', 18.3397, 109.7367, 620 UNION ALL
  SELECT '三亚', '大小洞天', 18.3040, 109.2140, 430
) a
JOIN (
  SELECT '悦享酒店' suffix, '悦享' brand, 5 star_level, 150 price_delta, 110 sold_count, '行政酒廊' facility, 16 address_no,
         'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80' cover_image UNION ALL
  SELECT '城景酒店', '城景', 4, 80, 92, '景观房', 28,
         'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '逸居酒店', '逸居', 4, 30, 78, '洗衣房', 39,
         'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '智选酒店', '智选', 3, -40, 66, '近地铁', 52,
         'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '度假酒店', '度假', 5, 200, 105, '亲子设施', 68,
         'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80'
) s
WHERE NOT EXISTS (
  SELECT 1
  FROM product p
  WHERE p.product_type = 'HOTEL'
    AND p.name = CONCAT(a.city, a.landmark, s.suffix)
);

INSERT INTO room_type (hotel_id, room_name, price, cancel_policy, breakfast)
SELECT p.id,
       r.room_name,
       p.price + r.price_delta,
       r.cancel_policy,
       r.breakfast
FROM product p
JOIN (
  SELECT '标准大床房' room_name, 0 price_delta, 'FREE_CANCEL' cancel_policy, 1 breakfast UNION ALL
  SELECT '高级双床房', 80, 'FREE_CANCEL', 1
) r
WHERE p.product_type = 'HOTEL'
  AND JSON_UNQUOTE(JSON_EXTRACT(p.extra, '$.seedBatch')) = 'HOTEL_SIMPLE_EXTRA_20260611'
  AND NOT EXISTS (
    SELECT 1
    FROM room_type rt
    WHERE rt.hotel_id = p.id
      AND rt.room_name = r.room_name
  );
