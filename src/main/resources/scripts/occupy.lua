-- occupy.lua
local key = KEYS[1]

-- 1. 检查 Key 是否存在
if redis.call('exists', key) == 0 then
    return -1
end

-- 2. 获取当前库存
local current = tonumber(redis.call('get', key))

-- 3. 判断库存
if current <= 0 then
    return 0
end

-- 4. 扣减
redis.call('decr', key)
return 1