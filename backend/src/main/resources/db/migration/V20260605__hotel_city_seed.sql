INSERT INTO product (product_type, name, price, stock, sold_count, status, extra, category_tags, create_time, update_time)
SELECT 'HOTEL', seed.name, seed.price, 40, 0, 1,
       JSON_OBJECT(
         'address', seed.address,
         'city', seed.city,
         'starLevel', seed.star_level,
         'brand', seed.brand,
         'coverImage', seed.cover_image,
         'facilities', JSON_ARRAY('早餐', '停车场', '健身房', seed.facility)
       ),
       JSON_ARRAY(seed.city, seed.brand, CONCAT(seed.star_level, '星')),
       NOW(), NOW()
FROM (
  SELECT '北京王府井饭店' name, '北京' city, '王府井大街88号' address, 5 star_level, 'Lightmark精选' brand, 720 price, '行政酒廊' facility, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80' cover_image UNION ALL
  SELECT '北京国贸云际酒店', '北京', '建国门外大街1号', 5, '云际', 880, '泳池', 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '北京后海雅居', '北京', '什刹海西岸16号', 4, '雅居', 520, '庭院', 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '北京望京商务酒店', '北京', '望京街9号', 4, '商旅', 460, '会议室', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '北京前门轻奢酒店', '北京', '前门东路20号', 3, '轻奢', 360, '洗衣房', 'https://images.unsplash.com/photo-1559599189-fe84dea4eb79?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '上海外滩酒店', '上海', '中山东一路', 5, 'Lightmark精选', 850, '江景', 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '上海陆家嘴云端酒店', '上海', '世纪大道100号', 5, '云际', 920, '泳池', 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '上海新天地精品酒店', '上海', '太仓路181号', 4, '雅居', 640, '酒吧', 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '上海虹桥商务酒店', '上海', '申虹路66号', 4, '商旅', 520, '会议室', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '上海静安轻居酒店', '上海', '南京西路88号', 3, '轻居', 430, '洗衣房', 'https://images.unsplash.com/photo-1559599189-fe84dea4eb79?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '成都太古里酒店', '成都', '中纱帽街8号', 5, 'Lightmark精选', 680, '下午茶', 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '成都锦江花园酒店', '成都', '滨江东路9号', 5, '花园', 620, '花园', 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '成都宽窄巷子雅居', '成都', '宽巷子18号', 4, '雅居', 480, '庭院', 'https://images.unsplash.com/photo-1559599189-fe84dea4eb79?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '成都高新商务酒店', '成都', '天府大道中段', 4, '商旅', 420, '会议室', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '成都熊猫主题酒店', '成都', '熊猫大道99号', 3, '主题', 360, '亲子', 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '杭州西湖湖畔酒店', '杭州', '北山街36号', 5, 'Lightmark精选', 760, '湖景', 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '杭州钱江新城酒店', '杭州', '富春路188号', 5, '云际', 690, '泳池', 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '杭州灵隐禅意酒店', '杭州', '灵隐路22号', 4, '雅居', 560, '茶室', 'https://images.unsplash.com/photo-1559599189-fe84dea4eb79?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '杭州武林商务酒店', '杭州', '体育场路88号', 4, '商旅', 450, '会议室', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '杭州运河轻居酒店', '杭州', '小河路12号', 3, '轻居', 330, '洗衣房', 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '三亚海棠湾度假酒店', '三亚', '海棠北路18号', 5, 'Lightmark精选', 980, '海景', 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '三亚亚龙湾海景酒店', '三亚', '亚龙湾路66号', 5, '海湾', 900, '泳池', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '三亚大东海亲子酒店', '三亚', '榆亚路26号', 4, '亲子', 620, '亲子', 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '三亚湾轻奢酒店', '三亚', '三亚湾路118号', 4, '轻奢', 520, '接送机', 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '三亚市中心商务酒店', '三亚', '解放路99号', 3, '商旅', 380, '洗衣房', 'https://images.unsplash.com/photo-1559599189-fe84dea4eb79?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '西安钟楼酒店', '西安', '东大街1号', 5, 'Lightmark精选', 560, '夜景', 'https://images.unsplash.com/photo-1564501049412-61c2a3083791?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '西安大唐不夜城酒店', '西安', '雁南一路9号', 5, '盛唐', 620, '亲子', 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '西安曲江雅居', '西安', '芙蓉西路18号', 4, '雅居', 460, '庭院', 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '西安高新商务酒店', '西安', '科技路88号', 4, '商旅', 390, '会议室', 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1200&q=80' UNION ALL
  SELECT '西安城墙轻居酒店', '西安', '环城南路16号', 3, '轻居', 300, '洗衣房', 'https://images.unsplash.com/photo-1559599189-fe84dea4eb79?auto=format&fit=crop&w=1200&q=80'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM product p WHERE p.product_type = 'HOTEL' AND p.name = seed.name
);

INSERT INTO room_type (hotel_id, room_name, price, cancel_policy, breakfast, bed_type, area)
SELECT p.id, room_seed.room_name, p.price + room_seed.price_delta, room_seed.cancel_policy, 1, room_seed.bed_type, room_seed.area
FROM product p
JOIN (
  SELECT '高级大床房' room_name, 0 price_delta, 'FREE_CANCEL' cancel_policy, '大床' bed_type, '32㎡' area UNION ALL
  SELECT '商务双床房', 80, 'FREE_CANCEL', '双床', '36㎡' UNION ALL
  SELECT '行政套房', 260, 'LIMITED_CANCEL', '大床', '58㎡'
) room_seed
WHERE p.product_type = 'HOTEL'
  AND p.name IN (
    '北京王府井饭店', '北京国贸云际酒店', '北京后海雅居', '北京望京商务酒店', '北京前门轻奢酒店',
    '上海外滩酒店', '上海陆家嘴云端酒店', '上海新天地精品酒店', '上海虹桥商务酒店', '上海静安轻居酒店',
    '成都太古里酒店', '成都锦江花园酒店', '成都宽窄巷子雅居', '成都高新商务酒店', '成都熊猫主题酒店',
    '杭州西湖湖畔酒店', '杭州钱江新城酒店', '杭州灵隐禅意酒店', '杭州武林商务酒店', '杭州运河轻居酒店',
    '三亚海棠湾度假酒店', '三亚亚龙湾海景酒店', '三亚大东海亲子酒店', '三亚湾轻奢酒店', '三亚市中心商务酒店',
    '西安钟楼酒店', '西安大唐不夜城酒店', '西安曲江雅居', '西安高新商务酒店', '西安城墙轻居酒店'
  )
  AND NOT EXISTS (
    SELECT 1 FROM room_type rt WHERE rt.hotel_id = p.id AND rt.room_name = room_seed.room_name
  );
