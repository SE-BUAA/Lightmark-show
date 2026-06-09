-- Debug flight seed data for local/manual testing.
-- Safe to run multiple times: each row is guarded by flight name.

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1001 北京大兴-上海浦东', 560.00, 42, 8, '["debug","flight","domestic"]', 1,
       '{"airline":"中国国际航空","flightNo":"DBG1001","departureCity":"BJS","departureAirport":"PKX","arrivalCity":"SHA","arrivalAirport":"PVG","departureDate":"2026-07-01","departureTime":"07:25","arrivalTime":"09:40","stops":0,"aircraft":"A321","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取10%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1001 北京大兴-上海浦东');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1002 北京首都-上海虹桥', 630.00, 18, 14, '["debug","flight","domestic"]', 1,
       '{"airline":"东方航空","flightNo":"DBG1002","departureCity":"BJS","departureAirport":"PEK","arrivalCity":"SHA","arrivalAirport":"SHA","departureDate":"2026-07-01","departureTime":"12:30","arrivalTime":"14:45","stops":0,"aircraft":"B737-800","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"FIRST","name":"头等舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取15%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1002 北京首都-上海虹桥');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1003 北京大兴-广州白云', 720.00, 26, 11, '["debug","flight","domestic"]', 1,
       '{"airline":"南方航空","flightNo":"DBG1003","departureCity":"BJS","departureAirport":"PKX","arrivalCity":"CAN","arrivalAirport":"CAN","departureDate":"2026-07-01","departureTime":"09:10","arrivalTime":"12:25","stops":0,"aircraft":"A320neo","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"特价舱按航司规则退改"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1003 北京大兴-广州白云');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1004 上海浦东-成都天府', 690.00, 33, 17, '["debug","flight","domestic"]', 1,
       '{"airline":"四川航空","flightNo":"DBG1004","departureCity":"SHA","departureAirport":"PVG","arrivalCity":"CTU","arrivalAirport":"TFU","departureDate":"2026-07-02","departureTime":"10:20","arrivalTime":"13:35","stops":0,"aircraft":"A330","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"起飞前4小时内收取30%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1004 上海浦东-成都天府');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1005 成都双流-深圳宝安', 510.00, 21, 9, '["debug","flight","domestic"]', 1,
       '{"airline":"深圳航空","flightNo":"DBG1005","departureCity":"CTU","departureAirport":"CTU","arrivalCity":"SZX","arrivalAirport":"SZX","departureDate":"2026-07-02","departureTime":"16:05","arrivalTime":"18:25","stops":0,"aircraft":"B737-900","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取12%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1005 成都双流-深圳宝安');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1006 广州白云-杭州萧山', 390.00, 56, 23, '["debug","flight","domestic"]', 1,
       '{"airline":"海南航空","flightNo":"DBG1006","departureCity":"CAN","departureAirport":"CAN","arrivalCity":"HGH","arrivalAirport":"HGH","departureDate":"2026-07-03","departureTime":"08:45","arrivalTime":"10:55","stops":0,"aircraft":"B787","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"按航司实时规则退改"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1006 广州白云-杭州萧山');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1007 深圳宝安-西安咸阳', 460.00, 12, 6, '["debug","flight","domestic"]', 1,
       '{"airline":"春秋航空","flightNo":"DBG1007","departureCity":"SZX","departureAirport":"SZX","arrivalCity":"XIY","arrivalAirport":"XIY","departureDate":"2026-07-03","departureTime":"21:15","arrivalTime":"23:55","stops":0,"aircraft":"A320","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"15kg","refundRule":"低价舱位退改费用较高"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1007 深圳宝安-西安咸阳');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1008 杭州萧山-北京首都', 580.00, 0, 19, '["debug","flight","domestic","soldout"]', 1,
       '{"airline":"中国国际航空","flightNo":"DBG1008","departureCity":"HGH","departureAirport":"HGH","arrivalCity":"BJS","arrivalAirport":"PEK","departureDate":"2026-07-04","departureTime":"18:40","arrivalTime":"20:55","stops":0,"aircraft":"A321","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"已售罄样例，用于验证库存过滤"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1008 杭州萧山-北京首都');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1009 上海虹桥-北京大兴', 540.00, 24, 31, '["debug","flight","domestic","inactive"]', 0,
       '{"airline":"吉祥航空","flightNo":"DBG1009","departureCity":"SHA","departureAirport":"SHA","arrivalCity":"BJS","arrivalAirport":"PKX","departureDate":"2026-07-04","departureTime":"06:50","arrivalTime":"09:05","stops":0,"aircraft":"A320","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"下架样例，用于验证状态过滤"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1009 上海虹桥-北京大兴');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1010 北京大兴-上海浦东 经停济南', 420.00, 15, 4, '["debug","flight","domestic","transfer"]', 1,
       '{"airline":"山东航空","flightNo":"DBG1010","departureCity":"BJS","departureAirport":"PKX","arrivalCity":"SHA","arrivalAirport":"PVG","departureDate":"2026-07-05","departureTime":"15:20","arrivalTime":"18:45","stops":1,"aircraft":"B737-800","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"经停样例，用于验证经停排序"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1010 北京大兴-上海浦东 经停济南');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1101 北京首都-上海虹桥 暑期早班', 610.00, 35, 12, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"中国国际航空","flightNo":"DBG1101","departureCity":"BJS","departureAirport":"PEK","arrivalCity":"SHA","arrivalAirport":"SHA","departureDate":"2026-08-15","departureTime":"08:05","arrivalTime":"10:20","stops":0,"aircraft":"A321","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取10%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1101 北京首都-上海虹桥 暑期早班');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1102 北京大兴-上海浦东 中秋特价', 480.00, 28, 15, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"东方航空","flightNo":"DBG1102","departureCity":"BJS","departureAirport":"PKX","arrivalCity":"SHA","arrivalAirport":"PVG","departureDate":"2026-09-18","departureTime":"19:35","arrivalTime":"21:50","stops":0,"aircraft":"B737-800","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取15%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1102 北京大兴-上海浦东 中秋特价');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1103 北京首都-上海虹桥 国庆高峰', 830.00, 40, 36, '["debug","flight","domestic","calendar","holiday"]', 1,
       '{"airline":"中国国际航空","flightNo":"DBG1103","departureCity":"BJS","departureAirport":"PEK","arrivalCity":"SHA","arrivalAirport":"SHA","departureDate":"2026-10-01","departureTime":"07:50","arrivalTime":"10:05","stops":0,"aircraft":"A330","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"FIRST","name":"头等舱"}],"baggage":"20kg","refundRule":"节假日高峰舱位按航司规则退改"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1103 北京首都-上海虹桥 国庆高峰');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1104 北京大兴-上海浦东 双十一低价', 450.00, 44, 18, '["debug","flight","domestic","calendar","lowprice"]', 1,
       '{"airline":"吉祥航空","flightNo":"DBG1104","departureCity":"BJS","departureAirport":"PKX","arrivalCity":"SHA","arrivalAirport":"PVG","departureDate":"2026-11-11","departureTime":"21:10","arrivalTime":"23:25","stops":0,"aircraft":"A320","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"低价舱位退改费用较高"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1104 北京大兴-上海浦东 双十一低价');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1105 北京首都-上海浦东 圣诞晚班', 780.00, 22, 20, '["debug","flight","domestic","calendar","holiday"]', 1,
       '{"airline":"东方航空","flightNo":"DBG1105","departureCity":"BJS","departureAirport":"PEK","arrivalCity":"SHA","arrivalAirport":"PVG","departureDate":"2026-12-24","departureTime":"20:15","arrivalTime":"22:30","stops":0,"aircraft":"B787","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取15%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1105 北京首都-上海浦东 圣诞晚班');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1106 北京大兴-上海虹桥 春运前', 520.00, 30, 16, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"中国联合航空","flightNo":"DBG1106","departureCity":"BJS","departureAirport":"PKX","arrivalCity":"SHA","arrivalAirport":"SHA","departureDate":"2027-01-18","departureTime":"10:45","arrivalTime":"13:00","stops":0,"aircraft":"B737-800","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取12%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1106 北京大兴-上海虹桥 春运前');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1107 北京首都-上海浦东 情人节', 690.00, 26, 21, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"海南航空","flightNo":"DBG1107","departureCity":"BJS","departureAirport":"PEK","arrivalCity":"SHA","arrivalAirport":"PVG","departureDate":"2027-02-14","departureTime":"13:20","arrivalTime":"15:40","stops":0,"aircraft":"B787","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"按航司实时规则退改"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1107 北京首都-上海浦东 情人节');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1108 北京大兴-上海浦东 春季低价', 500.00, 48, 10, '["debug","flight","domestic","calendar","lowprice"]', 1,
       '{"airline":"山东航空","flightNo":"DBG1108","departureCity":"BJS","departureAirport":"PKX","arrivalCity":"SHA","arrivalAirport":"PVG","departureDate":"2027-03-20","departureTime":"15:10","arrivalTime":"17:25","stops":0,"aircraft":"B737-800","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取10%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1108 北京大兴-上海浦东 春季低价');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1109 北京首都-上海虹桥 清明出行', 560.00, 32, 14, '["debug","flight","domestic","calendar","holiday"]', 1,
       '{"airline":"中国国际航空","flightNo":"DBG1109","departureCity":"BJS","departureAirport":"PEK","arrivalCity":"SHA","arrivalAirport":"SHA","departureDate":"2027-04-05","departureTime":"09:30","arrivalTime":"11:45","stops":0,"aircraft":"A321","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取10%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1109 北京首都-上海虹桥 清明出行');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1110 北京大兴-上海浦东 五一高峰', 740.00, 38, 33, '["debug","flight","domestic","calendar","holiday"]', 1,
       '{"airline":"东方航空","flightNo":"DBG1110","departureCity":"BJS","departureAirport":"PKX","arrivalCity":"SHA","arrivalAirport":"PVG","departureDate":"2027-05-01","departureTime":"11:55","arrivalTime":"14:10","stops":0,"aircraft":"A330","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"FIRST","name":"头等舱"}],"baggage":"20kg","refundRule":"节假日高峰舱位按航司规则退改"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1110 北京大兴-上海浦东 五一高峰');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1111 上海浦东-成都天府 暑期', 620.00, 36, 19, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"四川航空","flightNo":"DBG1111","departureCity":"SHA","departureAirport":"PVG","arrivalCity":"CTU","arrivalAirport":"TFU","departureDate":"2026-08-20","departureTime":"12:10","arrivalTime":"15:20","stops":0,"aircraft":"A330","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"起飞前4小时内收取30%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1111 上海浦东-成都天府 暑期');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1112 成都双流-深圳宝安 秋季', 470.00, 29, 11, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"深圳航空","flightNo":"DBG1112","departureCity":"CTU","departureAirport":"CTU","arrivalCity":"SZX","arrivalAirport":"SZX","departureDate":"2026-09-12","departureTime":"17:45","arrivalTime":"20:05","stops":0,"aircraft":"B737-900","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取12%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1112 成都双流-深圳宝安 秋季');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1113 广州白云-杭州萧山 秋游', 430.00, 50, 22, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"海南航空","flightNo":"DBG1113","departureCity":"CAN","departureAirport":"CAN","arrivalCity":"HGH","arrivalAirport":"HGH","departureDate":"2026-10-18","departureTime":"08:30","arrivalTime":"10:40","stops":0,"aircraft":"B787","cabins":[{"type":"ECONOMY","name":"经济舱"},{"type":"BUSINESS","name":"商务舱"}],"baggage":"20kg","refundRule":"按航司实时规则退改"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1113 广州白云-杭州萧山 秋游');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1114 深圳宝安-西安咸阳 初冬', 520.00, 18, 8, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"春秋航空","flightNo":"DBG1114","departureCity":"SZX","departureAirport":"SZX","arrivalCity":"XIY","arrivalAirport":"XIY","departureDate":"2026-11-06","departureTime":"21:30","arrivalTime":"00:10","stops":0,"aircraft":"A320","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"15kg","refundRule":"低价舱位退改费用较高"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1114 深圳宝安-西安咸阳 初冬');

INSERT INTO product (product_type, name, price, stock, sold_count, category_tags, status, extra)
SELECT 'FLIGHT', 'DBG1115 上海虹桥-北京大兴 跨年返程', 680.00, 34, 25, '["debug","flight","domestic","calendar"]', 1,
       '{"airline":"吉祥航空","flightNo":"DBG1115","departureCity":"SHA","departureAirport":"SHA","arrivalCity":"BJS","arrivalAirport":"PKX","departureDate":"2026-12-31","departureTime":"18:30","arrivalTime":"20:45","stops":0,"aircraft":"A320","cabins":[{"type":"ECONOMY","name":"经济舱"}],"baggage":"20kg","refundRule":"起飞前24小时外收取15%手续费"}'
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = 'DBG1115 上海虹桥-北京大兴 跨年返程');
