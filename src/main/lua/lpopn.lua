local key = KEYS[1]
local n = tonumber(ARGV[1])
local items = redis.call('lrange', key, 0, n - 1)
redis.call('ltrim', key, n, -1)
return items