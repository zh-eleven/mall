-- KEYS[1] SKU数据
-- KEYS[2] 用户购买数量
-- KEYS[3] 请求预扣记录
-- KEYS[4] 待确认请求索引
-- ARGV[1] requestId
-- ARGV[2] memberId
-- ARGV[3] quantity
-- ARGV[4] addressId

if redis.call('EXISTS', KEYS[1]) == 0 then
    return 5
end

local requestId = ARGV[1]
local memberId = ARGV[2]
local quantity = tonumber(ARGV[3])
local addressId = ARGV[4]

local existing = redis.call('HGET', KEYS[3], requestId)

if existing then
    local existingMemberId

    if string.sub(existing, 1, 2) == 'F:' then
        existingMemberId = string.sub(existing, 3)
    else
        existingMemberId = string.match(existing, '^([^:]+):')
    end

    if existingMemberId == memberId then
        return 6
    end

    return 5
end

local values = redis.call(
    'HMGET',
    KEYS[1],
    'stock',
    'perUserLimit',
    'startTime',
    'endTime'
)

if not values[1]
        or not values[2]
        or not values[3]
        or not values[4]
        or not quantity
        or quantity <= 0
        or not addressId then
    return 5
end

local stock = tonumber(values[1])
local perUserLimit = tonumber(values[2])
local startTime = tonumber(values[3])
local endTime = tonumber(values[4])

local redisTime = redis.call('TIME')
local now = tonumber(redisTime[1]) * 1000
        + math.floor(tonumber(redisTime[2]) / 1000)

if now < startTime then
    return 1
end

if now >= endTime then
    return 2
end

if stock < quantity then
    return 3
end

local purchased = tonumber(redis.call('HGET', KEYS[2], memberId)) or 0

if purchased + quantity > perUserLimit then
    return 4
end

local reservation = memberId
        .. ':' .. ARGV[3]
        .. ':' .. addressId
        .. ':' .. tostring(now)

redis.call('HINCRBY', KEYS[1], 'stock', -quantity)
redis.call('HINCRBY', KEYS[2], memberId, quantity)
redis.call('HSET', KEYS[3], requestId, reservation)
redis.call('ZADD', KEYS[4], now, requestId)

local ttl = redis.call('PTTL', KEYS[1])

if ttl > 0 then
    redis.call('PEXPIRE', KEYS[2], ttl)
    redis.call('PEXPIRE', KEYS[3], ttl)
    redis.call('PEXPIRE', KEYS[4], ttl)
end

return 0
