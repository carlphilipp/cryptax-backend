#!/bin/bash
set -ev
if [ "$TRAVIS_BRANCH" == "develop" ]; then
    docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD ;
    docker push cryptaxapp/cryptax-backend:$TRAVIS_BRANCH-$TRAVIS_COMMIT ;
elif [ "$TRAVIS_BRANCH" == "master" ]; then
    docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD ;
    docker push cryptaxapp/cryptax-backend:latest ;
fi