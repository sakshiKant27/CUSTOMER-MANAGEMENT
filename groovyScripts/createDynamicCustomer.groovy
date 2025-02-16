import org.apache.ofbiz.entity.Delegator
import org.apache.ofbiz.entity.DelegatorFactory
import org.apache.ofbiz.entity.GenericEntityException
import org.apache.ofbiz.entity.util.EntityQuery
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.base.util.Debug
import org.apache.ofbiz.service.ServiceUtil

def createCustomer() {
    Delegator delegator = (Delegator) context.get("delegator") ?: DelegatorFactory.getDelegator("default")
    String infoString = context.infoString
    String firstName = context.firstName
    String lastName = context.lastName

    if (!infoString || !firstName || !lastName) {
        return error("Missing required fields: infoString, firstName, lastName")
    }

    try {

        def existingCustomer = EntityQuery.use(delegator)
                .from("FindCustomerView")
                .where("infoString", infoString)
                .queryOne()

        if (existingCustomer) {
            return success(["partyId": existingCustomer.partyId, "message": "Customer already exists"])
        }


        def party = delegator.makeValue("Party")
        party.set("partyId", delegator.getNextSeqId("Party"))
        party.set("partyTypeId", "PERSON")
        party.create()


        def person = delegator.makeValue("Person")
        person.set("partyId", party.partyId)
        person.set("firstName", firstName)
        person.set("lastName", lastName)
        person.create()

        
        def partyRole = delegator.makeValue("PartyRole")
        partyRole.set("partyId", party.partyId)
        partyRole.set("roleTypeId", "CUSTOMER")
        partyRole.create()

        def contactMech = delegator.makeValue("ContactMech")
        contactMech.set("contactMechId", delegator.getNextSeqId("ContactMech"))
        contactMech.set("contactMechTypeId", "EMAIL_ADDRESS")
        contactMech.set("infoString", infoString)
        contactMech.create()


        def currDate = new Date()
        def partyContactMech = delegator.makeValue("PartyContactMech")
        partyContactMech.set("partyId", party.partyId)
        partyContactMech.set("contactMechId", contactMech.contactMechId)
        partyContactMech.set("fromDate", currDate)
        partyContactMech.create()

        return success(["message": "Customer created successfully", "partyId": party.partyId])

    } catch (GenericEntityException e) {

        return error("Error creating customer: " + e.getMessage())
    }
}