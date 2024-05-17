local pool = redis.call('sdiff', %s);
if next(pool) ~= nil  then
    if #pool < #KEYS then
        return nil;
    end;
    local r = {};
    for i = 3, #(KEYS) do
        local index = math.random(1, #pool);
        local x = pool[index];
        table.remove(pool, index);
        table.insert(r, x);
        %s
    end
	return r;
end;
return nil;