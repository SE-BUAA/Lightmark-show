UPDATE product
SET extra = JSON_SET(
    extra,
    '$.days',
    CAST(REGEXP_SUBSTR(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.days')), '[0-9]+') AS UNSIGNED)
)
WHERE product_type = 'VACATION'
  AND extra IS NOT NULL
  AND JSON_EXTRACT(extra, '$.days') IS NOT NULL
  AND REGEXP_SUBSTR(JSON_UNQUOTE(JSON_EXTRACT(extra, '$.days')), '[0-9]+') IS NOT NULL;
