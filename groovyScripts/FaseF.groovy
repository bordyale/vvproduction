/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.ofbiz.entity.condition.EntityExpr
import org.apache.ofbiz.entity.condition.EntityFunction
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.condition.EntityFieldValue
import org.apache.ofbiz.entity.condition.EntityConditionList
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.util.EntityUtil
import org.apache.ofbiz.base.util.UtilDateTime
import java.text.SimpleDateFormat
import org.apache.ofbiz.order.order.OrderReadHelper

import java.sql.Timestamp

def sdf = new SimpleDateFormat("yyyy-MM-dd")

import org.apache.ofbiz.entity.model.DynamicViewEntity

DynamicViewEntity dynamicViewEntity = new DynamicViewEntity()

fromDate = parameters.fildate1From
thruDate = parameters.fildate2From
productId = parameters.filproductId
filshowFaseF = parameters.filshowFaseF

List searchCond = []
if (fromDate) {
	def parseDate = sdf.parse(fromDate)
	searchCond.add(EntityCondition.makeCondition("date", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.toTimestamp(parseDate)))
}
if (thruDate) {
	def parseDate = sdf.parse(thruDate)
	searchCond.add(EntityCondition.makeCondition("date", EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.toTimestamp(parseDate)))
}
if (productId) {
	searchCond.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId))
}

List<HashMap<String,Object>> hashMaps = new ArrayList<HashMap<String,Object>>()

if(filshowFaseF.equals("Y")){
	faseItems = from("VvFaseF").where(searchCond).cache(false).queryList()

	faseItems = EntityUtil.orderBy(faseItems,  ["-date"])

	for (GenericValue entry: faseItems){
		Map<String,Object> e = new HashMap<String,Object>()
		e.put("faseId",entry.get("faseId"))
		e.put("productId",entry.get("productId"))
		e.put("quantity",entry.get("quantity"))
		e.put("date",entry.get("date"))
		e.put("worker",entry.get("worker"))
		e.put("scrap",entry.get("scrap"))
		e.put("shift",entry.get("shift"))
		e.put("sn",entry.get("sn"))


		hashMaps.add(e)
	}
}

context.listFaseF = hashMaps

