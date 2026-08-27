-- KEYS[1] SKU数据
-- KEYS[2] 用户购买数量
-- KEYS[3] 请求预扣记录
-- KEYS[4] 待确认请求索引
-- ARGV[1] requestId

if redis.call('EXISTS', KEYS[1]) == 0 then
    return 2
end

local reservation = redis.call('HGET', KEYS[3], ARGV[1])

-- 已回滚时清理可能残留的索引并直接成功，保证幂等。
if not reservation then
    redis.call('ZREM', KEYS[4], ARGV[1])
    return 0
end

-- 已经补偿完成的请求保留失败墓碑，禁止同一requestId重新预扣。
if string.sub(reservation, 1, 2) == 'F:' then
    redis.call('ZREM', KEYS[4], ARGV[1])
    return 0
end

local memberId, quantityText = string.match(
    reservation,
    '^([^:]+):([^:]+)'
)
local quantity = tonumber(quantityText)

if not memberId or not quantity then
    return 3
end

local purchased = tonumber(redis.call('HGET', KEYS[2], memberId)) or 0

if purchased < quantity then
    return 3
end

redis.call('HINCRBY', KEYS[1], 'stock', quantity)

local remaining = redis.call('HINCRBY', KEYS[2], memberId, -quantity)

if remaining <= 0 then
    redis.call('HDEL', KEYS[2], memberId)
end

redis.call('HSET', KEYS[3], ARGV[1], 'F:' .. memberId)
redis.call('ZREM', KEYS[4], ARGV[1])

return 1
