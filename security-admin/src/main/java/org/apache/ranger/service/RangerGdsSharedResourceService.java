/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ranger.service;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ranger.authorization.utils.JsonUtils;
import org.apache.ranger.common.GUIDUtil;
import org.apache.ranger.common.MessageEnums;
import org.apache.ranger.common.SearchField;
import org.apache.ranger.common.SortField;
import org.apache.ranger.entity.XXGdsDataShare;
import org.apache.ranger.entity.XXGdsSharedResource;
import org.apache.ranger.plugin.model.RangerGds.RangerSharedResource;
import org.apache.ranger.plugin.model.RangerPolicy;
import org.apache.ranger.plugin.model.RangerPolicyResourceSignature;
import org.apache.ranger.plugin.util.SearchFilter;
import org.apache.ranger.view.RangerGdsVList.RangerSharedResourceList;
import org.apache.ranger.view.VXMessage;
import org.apache.ranger.view.VXResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Scope("singleton")
public class RangerGdsSharedResourceService extends RangerGdsBaseModelService<XXGdsSharedResource, RangerSharedResource> {
    private static final Logger LOG = LoggerFactory.getLogger(RangerGdsSharedResourceService.class);

    @Autowired
    GUIDUtil guidUtil;

    public RangerGdsSharedResourceService() {
        super();

        searchFields.add(new SearchField(SearchFilter.SHARED_RESOURCE_NAME, "obj.name",        SearchField.DATA_TYPE.STRING,  SearchField.SEARCH_TYPE.FULL));
        searchFields.add(new SearchField(SearchFilter.SHARED_RESOURCE_ID,   "obj.id",          SearchField.DATA_TYPE.INTEGER, SearchField.SEARCH_TYPE.FULL));
        searchFields.add(new SearchField(SearchFilter.GUID      ,           "obj.guid",        SearchField.DATA_TYPE.STRING,  SearchField.SEARCH_TYPE.FULL));
        searchFields.add(new SearchField(SearchFilter.IS_ENABLED,           "obj.isEnabled",   SearchField.DATA_TYPE.BOOLEAN, SearchField.SEARCH_TYPE.FULL));
        searchFields.add(new SearchField(SearchFilter.DATA_SHARE_NAME,      "dsh.name",        SearchField.DATA_TYPE.STRING,  SearchField.SEARCH_TYPE.FULL, "XXGdsDataShare dsh", "obj.dataShareId = dsh.id"));
        searchFields.add(new SearchField(SearchFilter.DATA_SHARE_ID,        "obj.dataShareId", SearchField.DATA_TYPE.INTEGER, SearchField.SEARCH_TYPE.FULL));
        searchFields.add(new SearchField(SearchFilter.DATASET_NAME,         "d.name",          SearchField.DATA_TYPE.STRING,  SearchField.SEARCH_TYPE.FULL, "XXGdsDataShare dsh, XXGdsDataShareInDataset dshid, XXGdsDataset d", "obj.dataShareId = dsh.id and dsh.id = dshid.dataShareId and dshid.datasetId = d.id"));
        searchFields.add(new SearchField(SearchFilter.DATASET_ID,           "dshid.datasetId", SearchField.DATA_TYPE.INTEGER, SearchField.SEARCH_TYPE.FULL, "XXGdsDataShare dsh, XXGdsDataShareInDataset dshid", "obj.dataShareId = dsh.id and dsh.id = dshid.dataShareId"));
        searchFields.add(new SearchField(SearchFilter.PROJECT_NAME,         "p.name",          SearchField.DATA_TYPE.STRING,  SearchField.SEARCH_TYPE.FULL, "XXGdsDataShare dsh, XXGdsDataShareInDataset dshid, XXGdsDatasetInProject dip, XXGdsProject p", "obj.dataShareId = dsh.id and dsh.id = dshid.dataShareId and dshid.datasetId = dip.datasetId and dip.projectId = p.id"));
        searchFields.add(new SearchField(SearchFilter.PROJECT_ID,           "dip.projectId",   SearchField.DATA_TYPE.INTEGER, SearchField.SEARCH_TYPE.FULL, "XXGdsDataShare dsh, XXGdsDataShareInDataset dshid, XXGdsDatasetInProject dip", "obj.dataShareId = dsh.id and dsh.id = dshid.dataShareId and dshid.datasetId = dip.datasetId"));

        sortFields.add(new SortField(SearchFilter.CREATE_TIME,  "obj.createTime"));
        sortFields.add(new SortField(SearchFilter.UPDATE_TIME,  "obj.updateTime"));
        sortFields.add(new SortField(SearchFilter.DATASET_ID,   "obj.id", true, SortField.SORT_ORDER.ASC));
        sortFields.add(new SortField(SearchFilter.DATASET_NAME, "obj.name"));
    }

    @Override
    public RangerSharedResource postCreate(XXGdsSharedResource xObj) {
        RangerSharedResource ret = super.postCreate(xObj);

        // TODO:

        return ret;
    }

    @Override
    public RangerSharedResource postUpdate(XXGdsSharedResource xObj) {
        RangerSharedResource ret = super.postUpdate(xObj);

        // TODO:

        return ret;
    }

    @Override
    public XXGdsSharedResource preDelete(Long id) {
        // Update ServiceVersionInfo for each service in the zone
        XXGdsSharedResource ret = super.preDelete(id);

        // TODO:

        return ret;
    }

    @Override
    protected void validateForCreate(RangerSharedResource vObj) {
        List<VXMessage> msgList = null;

        if (vObj.getDataShareId() == null) {
            msgList = getOrCreateMessageList(msgList);

            msgList.add(MessageEnums.NO_INPUT_DATA.getMessage(null, "dataShareId"));
        }

        XXGdsDataShare xDataShare = daoMgr.getXXGdsDataShare().getById(vObj.getDataShareId());

        if (xDataShare == null) {
            msgList = getOrCreateMessageList(msgList);

            msgList.add(MessageEnums.NO_INPUT_DATA.getMessage(null, "dataShareId"));
        }

        if (CollectionUtils.isNotEmpty(msgList)) {
            VXResponse gjResponse = new VXResponse();

            gjResponse.setStatusCode(VXResponse.STATUS_ERROR);
            gjResponse.setMsgDesc("Validation failure");
            gjResponse.setMessageList(msgList);

            LOG.debug("Validation failure in createSharedResource({}): error={}", vObj, gjResponse);

            throw restErrorUtil.createRESTException(gjResponse);
        }

        if (StringUtils.isBlank(vObj.getGuid())) {
            vObj.setGuid(guidUtil.genGUID());
        }

        if (vObj.getIsEnabled() == null) {
            vObj.setIsEnabled(Boolean.TRUE);
        }
    }

    @Override
    protected void validateForUpdate(RangerSharedResource vObj, XXGdsSharedResource xObj) {
        List<VXMessage> msgList = null;

        if (vObj.getDataShareId() == null) {
            msgList = getOrCreateMessageList(msgList);

            msgList.add(MessageEnums.NO_INPUT_DATA.getMessage(null, "dataShareId"));
        }

        XXGdsDataShare xDataShare = daoMgr.getXXGdsDataShare().getById(vObj.getDataShareId());

        if (xDataShare == null) {
            msgList = getOrCreateMessageList(msgList);

            msgList.add(MessageEnums.NO_INPUT_DATA.getMessage(null, "dataShareId"));
        }

        if (!Objects.equals(vObj.getDataShareId(), xDataShare.getId())) {
            msgList = getOrCreateMessageList(msgList);

            msgList.add(MessageEnums.NO_INPUT_DATA.getMessage(null, "dataShareId"));
        }

        if (CollectionUtils.isNotEmpty(msgList)) {
            VXResponse gjResponse = new VXResponse();

            gjResponse.setStatusCode(VXResponse.STATUS_ERROR);
            gjResponse.setMsgDesc("Validation failure");
            gjResponse.setMessageList(msgList);

            LOG.debug("Validation failure in updateSharedResource({}): error={}", vObj, gjResponse);

            throw restErrorUtil.createRESTException(gjResponse);
        }

        if (vObj.getIsEnabled() == null) {
            vObj.setIsEnabled(Boolean.TRUE);
        }
    }

    @Override
    protected XXGdsSharedResource mapViewToEntityBean(RangerSharedResource vObj, XXGdsSharedResource xObj, int OPERATION_CONTEXT) {
        XXGdsDataShare xDataShare = daoMgr.getXXGdsDataShare().getById(vObj.getDataShareId());

        if (xDataShare == null) {
            throw restErrorUtil.createRESTException("No data share found with ID: " + vObj.getDataShareId(), MessageEnums.INVALID_INPUT_DATA);
        }

        xObj.setGuid(vObj.getGuid());
        xObj.setIsEnabled(vObj.getIsEnabled());
        xObj.setName(vObj.getName());
        xObj.setDescription(vObj.getDescription());
        xObj.setDataShareId(vObj.getDataShareId());
        xObj.setResource(JsonUtils.mapToJson(vObj.getResource()));
        xObj.setSubResourceNames(JsonUtils.listToJson(vObj.getSubResourceNames()));
        xObj.setResourceSignature(new RangerPolicyResourceSignature(vObj.getResource()).getSignature());
        xObj.setConditionExpr(vObj.getConditionExpr());
        xObj.setAccessTypes(JsonUtils.objectToJson(vObj.getAccessTypes()));
        xObj.setRowFilter(JsonUtils.objectToJson(vObj.getRowFilter()));
        xObj.setSubResourceMasks(JsonUtils.objectToJson(vObj.getSubResourceMasks()));
        xObj.setProfiles(JsonUtils.objectToJson(vObj.getProfiles()));
        xObj.setOptions(JsonUtils.mapToJson(vObj.getOptions()));
        xObj.setAdditionalInfo(JsonUtils.mapToJson(vObj.getAdditionalInfo()));

        return xObj;
    }

    @Override
    protected RangerSharedResource mapEntityToViewBean(RangerSharedResource vObj, XXGdsSharedResource xObj) {
        vObj.setGuid(xObj.getGuid());
        vObj.setIsEnabled(xObj.getIsEnabled());
        vObj.setVersion(xObj.getVersion());
        vObj.setName(xObj.getName());
        vObj.setDescription(xObj.getDescription());
        vObj.setDataShareId(xObj.getDataShareId());
        vObj.setResource(JsonUtils.jsonToMapPolicyResource(xObj.getResource()));
        vObj.setSubResourceNames(JsonUtils.jsonToListString(xObj.getSubResourceNames()));
        vObj.setConditionExpr(xObj.getConditionExpr());
        vObj.setAccessTypes(JsonUtils.jsonToSetString(xObj.getAccessTypes()));
        vObj.setRowFilter(JsonUtils.jsonToObject(xObj.getRowFilter(), RangerPolicy.RangerPolicyItemRowFilterInfo.class));
        vObj.setSubResourceMasks(JsonUtils.jsonToMapMaskInfo(xObj.getSubResourceMasks()));
        vObj.setProfiles(JsonUtils.jsonToSetString(xObj.getProfiles()));
        vObj.setOptions(JsonUtils.jsonToMapStringString(xObj.getOptions()));
        vObj.setAdditionalInfo(JsonUtils.jsonToMapStringString(xObj.getAdditionalInfo()));

        return vObj;
    }

    public RangerSharedResource getPopulatedViewObject(XXGdsSharedResource xObj) {
        return this.populateViewBean(xObj);
    }

    public RangerSharedResourceList searchSharedResources(SearchFilter filter) {
        LOG.debug("==> searchSharedResources({})", filter);

        RangerSharedResourceList  ret      = new RangerSharedResourceList();
        List<XXGdsSharedResource> datasets = super.searchResources(filter, searchFields, sortFields, ret);

        if (datasets != null) {
            for (XXGdsSharedResource dataset : datasets) {
                ret.getList().add(getPopulatedViewObject(dataset));
            }
        }

        LOG.debug("<== searchSharedResources({}): ret={}", filter, ret);

        return ret;
    }
}
