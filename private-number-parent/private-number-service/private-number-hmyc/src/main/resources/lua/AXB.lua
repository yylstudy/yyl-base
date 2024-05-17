local index = tonumber(ARGV[1]);
local pool = redis.call('sdiff', KEYS[1],KEYS[2],KEYS[3]);
if next(pool) ~= nil  then
    if index > #pool then
        index = math.random(1, #pool);
    end;
    local x = pool[index];
    redis.call('sadd', KEYS[2], x);
    redis.call('sadd', KEYS[3], x);
	return x;
end;
return nil;

