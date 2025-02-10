import org.apache.ofbiz.entity.EntityCondition
import org.apache.ofbiz.entity.EntityFind
import org.apache.ofbiz.context.ExecutionContext
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.EntityList
import org.apache.ofbiz.service.ServiceUtil

def findCustomer(DispatchContext dctx, Map<String, ? extends Object> context) {
    // Extracting the filters from the context map
    String emailAddress = context.emailAddress
    String firstName = context.firstName
    String lastName = context.lastName
    String contactNumber = context.contactNumber
    String postalAddress = context.postalAddress

    // Prepare a list to hold the matching partyIds
    List<String> partyIdList = []

    // Get the ExecutionContext and create an EntityFind object
    ExecutionContext ec = dctx.getExecutionContext()
    EntityFind ef = ec.entity.find("org.apache.ofbiz.customer.FindCustomerView")

    // Apply conditions based on the input filters (case-insensitive, partial match)
    if (firstName) {
        ef.condition('firstName', EntityCondition.LIKE, "%" + firstName + "%")
    }
    if (lastName) {
        ef.condition('lastName', EntityCondition.LIKE, "%" + lastName + "%")
    }
    if (contactNumber) {
        ef.condition('contactNumber', EntityCondition.LIKE, "%" + contactNumber + "%")
    }
    if (postalAddress) {
        ef.condition('postalAddress', EntityCondition.LIKE, "%" + postalAddress + "%")
    }
    if (emailAddress) {
        ef.condition('infoString', EntityCondition.LIKE, "%" + emailAddress + "%")
    }

    // Sort by firstName and lastName
    ef.orderBy("firstName, lastName")

    // Execute the query
    EntityList el = ef.list()

    // Collect the partyIds of the matched customers
    el.each { GenericValue ev ->
        partyIdList.add(ev.partyId)
    }

    // Prepare the result to return
    Map<String, Object> result = ServiceUtil.returnSuccess()
    result.put("partyIdList", partyIdList)

    return result
}
