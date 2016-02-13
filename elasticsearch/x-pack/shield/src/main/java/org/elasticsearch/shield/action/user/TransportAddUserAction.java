/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.shield.action.user;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.shield.authc.esnative.ESNativeUsersStore;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class TransportAddUserAction extends HandledTransportAction<PutUserRequest, PutUserResponse> {

    private final ESNativeUsersStore usersStore;

    @Inject
    public TransportAddUserAction(Settings settings, ThreadPool threadPool, ActionFilters actionFilters,
                                  IndexNameExpressionResolver indexNameExpressionResolver,
                                  ESNativeUsersStore usersStore, TransportService transportService) {
        super(settings, PutUserAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, PutUserRequest::new);
        this.usersStore = usersStore;
    }

    @Override
    protected void doExecute(final PutUserRequest request, final ActionListener<PutUserResponse> listener) {
        usersStore.putUser(request, new ActionListener<Boolean>() {
            @Override
            public void onResponse(Boolean created) {
                if (created) {
                    logger.info("added user [{}]", request.username());
                } else {
                    logger.info("updated user [{}]", request.username());
                }
                listener.onResponse(new PutUserResponse(created));
            }

            @Override
            public void onFailure(Throwable e) {
                logger.error("failed to add user: ", e);
                listener.onFailure(e);
            }
        });
    }
}
