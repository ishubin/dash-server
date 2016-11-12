#!/bin/bash

export API=http://localhost:4567/api

post_json() {
    curl -X POST "$API/$1" -H "Content-Type: application/json" -d "$2"
}

