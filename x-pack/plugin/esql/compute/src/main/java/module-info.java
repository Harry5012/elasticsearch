/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

module org.elasticsearch.compute {
    requires org.apache.lucene.core;
    requires org.elasticsearch.base;
    requires org.elasticsearch.server;
    requires org.elasticsearch.compute.ann;
    requires org.elasticsearch.xcontent;
    requires t.digest;
    requires org.apache.logging.log4j;

    exports org.elasticsearch.compute;
    exports org.elasticsearch.compute.aggregation;
    exports org.elasticsearch.compute.data;
    exports org.elasticsearch.compute.lucene;
    exports org.elasticsearch.compute.operator;
    exports org.elasticsearch.compute.operator.exchange;
    exports org.elasticsearch.compute.aggregation.blockhash;
}
