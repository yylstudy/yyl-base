local pool = redis.call('sdiff', KEYS[1],KEYS[2],KEYS[3]);
if next(pool) ~= nil  then
    if #pool < #KEYS then
        return nil;
    end;
    local r = {};
    for i=3, #(KEYS) do
        local index = math.random(1, #pool);
        local x = pool[index];
        table.remove(pool, index);
        table.insert(r, x);
        redis.call('sadd', KEYS[2], x);
        redis.call('sadd', KEYS[3], x);
    end;
	return r;
end;
return nil;