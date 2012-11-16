#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#  shortener.py
#  web-kata
#

"""
A basic URL shortener
"""

import math
import urlparse
import hashlib

import flask
import pymongo


HOST = 'http://127.0.0.1:5000/'


db = pymongo.Connection().pyshortener
app = flask.Flask(__name__)


@app.route('/create', methods=['POST'])
def create():
    args = flask.request.form
    if 'url' not in args:
        raise flask.exceptions.BadRequest('must POST with a url parameter')

    url = args['url']
    if len(url) >= 2048:
        raise flask.exceptions.BadRequest('URL must be no longer than 2048'
                ' characters')

    scheme = urlparse.urlparse(url).scheme
    if scheme not in ('http', 'https', 'ftp'):
        raise flask.exceptions.BadRequest('need an http/https/ftp URI')

    match = db.urls.find_one({'_id': url})
    short_url = match['short_url'] if match else gen_short_url(url)

    return flask.Response(HOST + short_url, status=201)


@app.route('/<short_url>', methods=['GET'])
def redirect(short_url=None):
    if not short_url:
        return flask.abort(404)

    match = db.urls.find_one({'short_url': short_url}, fields=[])
    if match:
        return flask.redirect(match['_id'])

    return flask.abort(404)


def gen_short_url(url):
    for candidate in iter_shortened(url):
        if not db.urls.find_one({'short_url': candidate}, fields=[]):
            db.urls.save({'_id': url, 'short_url': candidate})
            return candidate

    raise ValueError("couldn't find a free spot to save to")


def iter_shortened(url):
    h = hashlib.md5(url)
    max_chars = int(math.ceil(math.log(2) * 8 * h.digest_size / math.log(62)))
    n = sum([j * (256 ** i) for (i, j) in enumerate(map(ord, h.digest()))])
    cs = []
    for i in xrange(max_chars):
        n, c = divmod(n, 62)
        cs.append(c)
    digest62 = ''.join(to_code(c) for c in reversed(cs))

    for i in xrange(2, len(digest62) + 1):
        yield digest62[:i]


def to_code(c):
    if c < 10:
        return chr(ord('0') + c)

    if c < 36:
        return chr(ord('a') + c - 10)

    if c >= 62:
        raise ValueError(c)

    return chr(ord('A') + c - 36)


if __name__ == '__main__':
    app.run(debug=True)
