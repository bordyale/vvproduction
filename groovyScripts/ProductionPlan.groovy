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
orderShipBeforeFrom = parameters.orderShipBeforeFrom
orderShipBeforeTo = parameters.orderShipBeforeTo
partyIdTo = parameters.filpartnerId
productId = parameters.filproductId
filProductionPlan = parameters.filProductionPlan 



List filCond= []
List activCond = []
	activCond.add(EntityCondition.makeCondition("quantityShippable", EntityOperator.EQUALS, null))
	activCond.add(EntityCondition.makeCondition("quantityShippable", EntityOperator.GREATER_THAN, BigDecimal.ZERO))
if (partyIdTo) {
	filCond.add(EntityCondition.makeCondition("partnerId", EntityOperator.EQUALS, partyIdTo))
}
if (productId) {
	filCond.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId))
}

activCondOR = EntityCondition.makeCondition(activCond,EntityOperator.OR)
filCondAND = EntityCondition.makeCondition(filCond, EntityOperator.AND)
searchCond2 = EntityCondition.makeCondition([filCondAND, activCondOR], EntityOperator.AND)
notShippedItems = select("orderId","orderItemSeqId","quantity","quantityShipped","productId","name","shipBeforeDate","weight","orderName","orderDate").from("VvOrderItemShippingItemView").where(searchCond2).cache(false).queryList()

notShippedItems = EntityUtil.orderBy(notShippedItems,  ["shipBeforeDate"])

List<HashMap<String,Object>> hashMaps2 = new ArrayList<HashMap<String,Object>>()
if(filProductionPlan.equals("Y")){

	
	Map<String,Object> whChecked = new HashMap<String,Object>()
	warehouse= from("VvWarehouseQtySumView").cache(false).queryList()
	for (GenericValue entry: warehouse){
		whChecked.put(entry.get("productId"),entry.get("quantitySum"))
		System.out.println("warehouse: " + entry.get("productId") + " - " + entry.get("quantitySum"))
	}


	int i=0
	BigDecimal progresNetWeigh = BigDecimal.ZERO
	for (GenericValue entry: notShippedItems){
		BigDecimal quantity = entry.get("quantity")
		BigDecimal quantityShipped = entry.get("quantityShipped")
		String productId = entry.get("productId")
		if (quantityShipped ==null){
			quantityShipped = BigDecimal.ZERO
		}
		BigDecimal whAvailable = BigDecimal.ZERO
		BigDecimal quantityShippable =quantity.subtract(quantityShipped)
		BigDecimal whQuantity = whChecked.get(productId)	
		System.out.println("whQuantity: " + whQuantity)
		if (whQuantity){
			if (whQuantity<=quantityShippable){
				whAvailable = whQuantity	
			    whChecked.put(productId,BigDecimal.ZERO)	
			}else{
				whAvailable = quantityShippable
				whQuantity = whQuantity.subtract(quantityShippable)
			    whChecked.put(productId,whQuantity)	

			}

		}


		Map<String,Object> e = new HashMap<String,Object>()
		e.put("whAvailable",whAvailable)
		e.put("orderItemSeqId",entry.get("orderItemSeqId"))
		e.put("orderId",entry.get("orderId"))
		e.put("orderName",entry.get("orderName"))
		e.put("productId",productId)
		e.put("orderDate",entry.get("orderDate"))
		e.put("shipBeforeDate",entry.get("shipBeforeDate"))
		e.put("name",entry.get("name"))
		e.put("quantity",entry.get("quantity"))
		e.put("weight",entry.get("weight"))
		e.put("quantityShipped",quantityShipped)
		e.put("quantityShippable",quantityShippable)

		BigDecimal productWeight = entry.get("weight")
		BigDecimal netWeight = quantityShippable.multiply(productWeight)
		e.put("netWeight",netWeight)
		progresNetWeigh = progresNetWeigh.add(netWeight)
		e.put("progresNetWeight",progresNetWeigh)
		hashMaps2.add(e)
	}

}

context.orderItems2 = hashMaps2
