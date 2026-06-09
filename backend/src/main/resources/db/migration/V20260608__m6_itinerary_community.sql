CREATE TABLE IF NOT EXISTS `travel_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) DEFAULT '',
  `destination` VARCHAR(100) NOT NULL,
  `start_date` DATE DEFAULT NULL,
  `end_date` DATE DEFAULT NULL,
  `plan_data` JSON DEFAULT NULL,
  `is_public` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_travel_plan_user_id` (`user_id`),
  KEY `idx_travel_plan_public_time` (`is_public`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` TEXT,
  `images` JSON DEFAULT NULL,
  `likes` INT DEFAULT 0,
  `comments_count` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_user_id` (`user_id`),
  KEY `idx_post_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `post_like` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
  KEY `idx_post_like_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `target_type` VARCHAR(20) NOT NULL,
  `target_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `parent_id` BIGINT DEFAULT NULL,
  `content` VARCHAR(1000) NOT NULL,
  `likes` INT DEFAULT 0,
  `is_approved` TINYINT DEFAULT 1,
  `ip` VARCHAR(45) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_comment_target` (`target_type`, `target_id`),
  KEY `idx_comment_parent_id` (`parent_id`),
  KEY `idx_comment_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `answer` VARCHAR(1000) DEFAULT NULL,
  `answer_user_id` BIGINT DEFAULT NULL,
  `status` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `answer_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_question_user_id` (`user_id`),
  KEY `idx_question_status_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `travel_plan` (`user_id`, `title`, `destination`, `start_date`, `end_date`, `plan_data`, `is_public`)
SELECT 2, '杭州三日慢游', '杭州', CURRENT_DATE + INTERVAL 7 DAY, CURRENT_DATE + INTERVAL 9 DAY,
       JSON_ARRAY(
         JSON_OBJECT('day', 1, 'theme', '西湖初见', 'items', JSON_ARRAY('抵达杭州', '漫步白堤', '湖滨晚餐')),
         JSON_OBJECT('day', 2, 'theme', '茶山与古寺', 'items', JSON_ARRAY('龙井村品茶', '灵隐寺', '河坊街夜游')),
         JSON_OBJECT('day', 3, 'theme', '城市收尾', 'items', JSON_ARRAY('京杭大运河', '小河直街', '返程'))
       ),
       1
WHERE NOT EXISTS (SELECT 1 FROM `travel_plan` WHERE `title` = '杭州三日慢游');

INSERT INTO `post` (`user_id`, `title`, `content`, `images`, `likes`, `comments_count`, `status`)
SELECT 2, '杭州三日慢游路线分享', '西湖适合留出完整半天，龙井村建议上午去，光线和人流都更舒服。', JSON_ARRAY('https://picsum.photos/id/104/640/420'), 2, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `post` WHERE `title` = '杭州三日慢游路线分享');

INSERT INTO `post` (`user_id`, `title`, `content`, `images`, `likes`, `comments_count`, `status`)
SELECT 3, '三亚亲子游避坑小记', '带小朋友去海边一定要预留午休时间，蜈支洲岛水质不错，但防晒要做足。', JSON_ARRAY('https://picsum.photos/id/15/640/420'), 1, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM `post` WHERE `title` = '三亚亲子游避坑小记');

INSERT INTO `comment` (`target_type`, `target_id`, `user_id`, `content`)
SELECT 'POST', p.id, 3, '这条路线很实用，已收藏。'
FROM `post` p
WHERE p.title = '杭州三日慢游路线分享'
  AND NOT EXISTS (
    SELECT 1 FROM `comment` c WHERE c.target_type = 'POST' AND c.target_id = p.id AND c.content = '这条路线很实用，已收藏。'
  );

INSERT INTO `question` (`user_id`, `title`, `content`, `answer`, `answer_user_id`, `status`, `answer_time`)
SELECT 2, '第一次去成都住哪里方便？', '想吃小吃、坐地铁方便，预算中等。', '可以优先看春熙路、宽窄巷子或太古里周边，交通和餐饮都比较集中。', 1, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `question` WHERE `title` = '第一次去成都住哪里方便？');
