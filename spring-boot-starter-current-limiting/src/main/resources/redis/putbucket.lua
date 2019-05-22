--current-limiting-bucket
local key1 = KEYS[1]
--bucket-size
local key2 = tonumber(KEYS[2])
--bucket-put-key-key
local key3 = KEYS[3]
--bucket-put-key-value
local key4 = KEYS[4]
--bucket-count
local s = tonumber(redis.call("get", key1))
--method
if (s < 0)
then
    redis.call("set", key1, "1")
elseif (s < key2)
then
    redis.call("incrby", key1, 1)
end
redis.call("set", key3, key4)
return true