# Python URL shortener

It uses the prefix of an md5 hash, squeezed into a base62 digest, as the shortened URL for something. This scheme is less efficient than keeping a sequence and greedily filling the smaller string lengths, but it's conceptually easier.
