INSERT INTO orders (
  order_no, user_id, order_type, total_amount, points_deduct, pay_amount,
  payment_method, source, status, pay_time, create_time, update_time
)
SELECT
  CONCAT('HR', hotel.id, LPAD(tpl.seq, 2, '0')) AS order_no,
  COALESCE((SELECT MIN(u.id) FROM `user` u), 1) AS user_id,
  'HOTEL' AS order_type,
  hotel.price AS total_amount,
  0 AS points_deduct,
  hotel.price AS pay_amount,
  'SEED' AS payment_method,
  'REVIEW_SEED' AS source,
  2 AS status,
  DATE_SUB(NOW(), INTERVAL tpl.seq DAY) AS pay_time,
  DATE_SUB(NOW(), INTERVAL tpl.seq DAY) AS create_time,
  DATE_SUB(NOW(), INTERVAL tpl.seq DAY) AS update_time
FROM (
  SELECT p.id, p.name, COALESCE(p.price, 300) AS price
  FROM product p
  WHERE p.product_type = 'HOTEL'
    AND p.status = 1
) hotel
JOIN (
  SELECT 1 AS seq UNION ALL SELECT 2 UNION ALL SELECT 3
  UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
) tpl
WHERE NOT EXISTS (
  SELECT 1 FROM orders o
  WHERE o.order_no = CONCAT('HR', hotel.id, LPAD(tpl.seq, 2, '0'))
);

INSERT INTO review (order_id, product_id, user_id, rating, content, images, status, create_time)
SELECT
  o.id AS order_id,
  hotel.id AS product_id,
  o.user_id AS user_id,
  tpl.rating,
  REPLACE(REPLACE(tpl.content_tpl, '{hotel}', hotel.name), '{city}', hotel.city) AS content,
  '[]' AS images,
  1 AS status,
  o.create_time
FROM (
  SELECT
    p.id,
    p.name,
    COALESCE(
      JSON_UNQUOTE(JSON_EXTRACT(p.extra, '$.city')),
      CASE
        WHEN p.name LIKE '%北京%' THEN '北京'
        WHEN p.name LIKE '%上海%' THEN '上海'
        WHEN p.name LIKE '%成都%' THEN '成都'
        WHEN p.name LIKE '%杭州%' THEN '杭州'
        WHEN p.name LIKE '%三亚%' THEN '三亚'
        WHEN p.name LIKE '%西安%' THEN '西安'
        ELSE '当地'
      END
    ) AS city
  FROM product p
  WHERE p.product_type = 'HOTEL'
    AND p.status = 1
) hotel
JOIN (
  SELECT 1 AS seq, 5 AS rating,
         '这次入住{hotel}整体很满意，位置在{city}出行很方便，前台办理入住速度快，房间干净整洁，床品也比较舒服。晚上休息安静，第二天退房也顺畅，适合第一次来{city}旅行的人。' AS content_tpl
  UNION ALL
  SELECT 2, 4,
         '{hotel}的性价比不错，房间面积比预期大，卫生间没有异味，热水稳定。早餐种类不算特别多，但中西式都有，补餐也比较及时。服务人员态度很好，有问题响应比较快。'
  UNION ALL
  SELECT 3, 5,
         '带家人来{city}玩选了{hotel}，体验挺省心。酒店周边吃饭和打车都方便，房间隔音比预想好，孩子休息得不错。客房打扫细致，洗漱用品补得及时，下次来还会考虑。'
  UNION ALL
  SELECT 4, 4,
         '{hotel}比较适合商务出行，网络稳定，桌面空间够用，晚上处理工作不受影响。房间设施维护得还可以，床垫偏软但能接受。高峰期电梯稍微等了一会儿，其他方面都不错。'
  UNION ALL
  SELECT 5, 3,
         '客观说{hotel}位置和服务都还可以，前台沟通顺畅，房间卫生也过关。不过临街房间偶尔能听到车辆声音，睡眠浅的人建议提前备注安静房。整体属于可以接受的水平。'
  UNION ALL
  SELECT 6, 5,
         '入住{hotel}最大的感受是细节做得比较好，房间没有明显灰尘，空调温度稳定，浴室水压也足。工作人员会主动提醒早餐时间和周边路线，整个入住过程比较轻松。'
) tpl
JOIN orders o
  ON o.order_no = CONCAT('HR', hotel.id, LPAD(tpl.seq, 2, '0'))
WHERE NOT EXISTS (
  SELECT 1
  FROM review r
  WHERE r.product_id = hotel.id
    AND r.content = REPLACE(REPLACE(tpl.content_tpl, '{hotel}', hotel.name), '{city}', hotel.city)
);
