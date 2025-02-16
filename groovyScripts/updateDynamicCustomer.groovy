import org.apache.ofbiz.entity.Delegator
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.GenericEntityException
import java.math.BigDecimal
import java.sql.Timestamp
import org.apache.ofbiz.entity.DelegatorFactory
import org.apache.ofbiz.base.util.Debug
import org.apache.ofbiz.entity.Delegator

def UpdateCustomerDetail() {
    ExecutionContext ec = context.ec
    String infoString = context.infoString
    String postalAddress = context.postalAddress
    String phoneNumber = context.phoneNumber
    String countryCode = context.countryCode
    String areaCode = context.areaCode
    String toName = context.toName
    String attnName = context.attnName
    String address1 = context.address1
    String address2 = context.address2
    String city = context.city
    String postalCode = context.postalCode

    if (!infoString) {
        return error("Missing required field: infoString")
    }

    def resultEmail = ec.service.sync().name("PartyServices.find#Customer")
            .parameters(["infoString": infoString])
            .call()

    if (!resultEmail.get("customerList") || resultEmail.get("customerList").isEmpty()) {
        return ServiceUtil.returnError("Customer with email ${infoString} not found")
    }

    String partyId = resultEmail.get("customerList").get(0)


    if (phoneNumber || countryCode || areaCode) {
        EntityFind phoneFind = ec.entity.find("moqui.PartyContactMech")
        phoneFind.condition("partyId", partyId)
        phoneFind.condition("contactMechPurposeEnumId", "PhonePrimary")

        EntityList phoneList = phoneFind.list()


        for (EntityValue ev in phoneList) {
            if (!ev.isFieldSet("thruDate")) {
                ev.set("thruDate", new Date()).update()
            }
        }


        EntityValue contactMech = ec.entity.makeValue("moqui.ContactMech")
                .setAll(["infoString": phoneNumber,
                         "contactMechTypeEnumId": "TELECOM_NUMBER"])
                .setSequencedIdPrimary()
                .create()


        ec.entity.makeValue("moqui.TelecomNumber")
                .setAll(["contactMechId": contactMech.contactMechId,
                         "countryCode": countryCode,
                         "areaCode": areaCode,
                         "contactNumber": phoneNumber])
                .create()


        ec.entity.makeValue("moqui.PartyContactMech")
                .setAll(["partyId": partyId,
                         "contactMechId": contactMech.contactMechId,
                         "contactMechPurposeEnumId": "PhonePrimary",
                         "fromDate": new Date()])
                .create()
    }


    if (toName || attnName || address1 || address2 || city || postalCode) {
        EntityFind postalFind = ec.entity.find("moqui.PartyContactMech")
        postalFind.condition("partyId", partyId)
        postalFind.condition("contactMechPurposeEnumId", "PostalPrimary")

        EntityList postalList = postalFind.list()


        for (EntityValue ev in postalList) {
            if (!ev.isFieldSet("thruDate")) {
                ev.set("thruDate", new Date()).update()
            }
        }

        EntityValue contactMech = ec.entity.makeValue("moqui.ContactMech")
                .setAll(["infoString": postalAddress,
                         "contactMechTypeEnumId": "POSTAL_ADDRESS"])
                .setSequencedIdPrimary()
                .create()


        ec.entity.makeValue("moqui.PostalAddress")
                .setAll(["contactMechId": contactMech.contactMechId,
                         "toName": toName,
                         "attnName": attnName,
                         "address1": address1,
                         "address2": address2,
                         "city": city,
                         "postalCode": postalCode])
                .create()


        ec.entity.makeValue("moqui.PartyContactMech")
                .setAll(["partyId": partyId,
                         "contactMechId": contactMech.contactMechId,
                         "contactMechPurposeEnumId": "PostalPrimary",
                         "fromDate": new Date()])
                .create()
    }

    return success(["message": "Customer updated successfully", "partyId": partyId])
}