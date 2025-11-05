package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.budgee.enums.Currency;
import com.budgee.model.*;
import com.budgee.service.EmailService;
import com.budgee.service.lookup.GroupLookup;
import com.budgee.service.lookup.GroupMemberLookup;
import com.budgee.service.lookup.GroupTransactionLookup;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "SENDGRID-EMAIL-SERVICE")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SendGridEmailServiceImpl implements EmailService {

    // -------------------------------------------------------------------
    // SERVICES
    // -------------------------------------------------------------------
    SendGrid sendGrid;

    // -------------------------------------------------------------------
    // LOOKUP
    // -------------------------------------------------------------------
    GroupLookup groupLookup;
    GroupTransactionLookup groupTransactionLookup;
    GroupMemberLookup groupMemberLookup;

    // -------------------------------------------------------------------
    // PRIVATE FIELDS
    // -------------------------------------------------------------------
    @NonFinal
    @Value("${spring.application.url}")
    String BASE_URL;

    @NonFinal
    @Value("${spring.mail.from}")
    String MAIL_FROM;

    @NonFinal
    @Value("${spring.sendgrid.register-template}")
    String REGISTER_TEMPLATE_EMAIL;

    @NonFinal
    @Value("${spring.sendgrid.group-transaction-template}")
    String GROUP_TRANSACTION_TEMPLATE_EMAIL;

    @Async("mailExecutor")
    @Override
    public void sendRegisterEmail(
            String toEmail, String fullName, String verificationLink, String verificationToken) {

        log.info("[sendRegisterEmail] toEmail={}", toEmail);

        Map<String, Object> templateData =
                Map.of(
                        "full_name", fullName,
                        "verification_token", verificationToken);

        Mail mail = buildMail(toEmail, REGISTER_TEMPLATE_EMAIL, templateData);

        sendMailToSendGrid(mail, "register");
    }

    @Override
    public void sendForgetPassword() {}

    @Async("mailExecutor")
    @Override
    public void sendGroupTransactionCreatedEmail(UUID groupId, UUID transactionId) {
        log.info(
                "[sendGroupTransactionCreatedEmail] groupId={} transactionId={}",
                groupId,
                transactionId);

        Group group = groupLookup.getGroupById(groupId);
        GroupTransaction transaction =
                groupTransactionLookup.getGroupTransactionById(transactionId);
        GroupMember creator = transaction.getMember();
        List<GroupMember> groupMembers = groupMemberLookup.getAllGroupMembersByGroupId(groupId);

        List<String> emails =
                groupMembers.stream()
                        .map(GroupMember::getUser)
                        .filter(Objects::nonNull)
                        .map(User::getEmail)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList();

        if (emails.isEmpty()) {
            log.warn(
                    "[sendGroupTransactionCreatedEmail] No valid recipients found for groupId={}",
                    groupId);
            return;
        }

        Map<String, Object> data =
                Map.of(
                        "creatorName", creator.getMemberName(),
                        "note", Optional.ofNullable(transaction.getNote()).orElse(""),
                        "transactionType",
                                Optional.ofNullable(transaction.getType())
                                        .map(Enum::name)
                                        .orElse("UNKNOWN"),
                        "amount",
                                transaction.getAmount() != null
                                        ? transaction.getAmount().toPlainString()
                                        : "0",
                        "currency", Currency.VND.name(),
                        "date", transaction.getDate().toString(),
                        "groupName", group.getName(),
                        "viewLink", buildGroupTransactionUrl(groupId, transactionId));

        emails.forEach(
                email -> {
                    Mail mail = buildMail(email, GROUP_TRANSACTION_TEMPLATE_EMAIL, data);
                    log.info("[sendGroupTransactionCreatedEmail] sending mail to {}", email);
                    sendMailToSendGrid(mail, "group transaction created");
                });
    }

    // -------------------------------------------------------------------
    // PRIVATE HELPERS
    // -------------------------------------------------------------------

    Mail buildMail(String toEmail, String templateId, Map<String, Object> dynamicData) {
        Email to = new Email(toEmail);
        Email from = new Email(MAIL_FROM);

        Personalization personalization = new Personalization();
        personalization.addTo(to);

        dynamicData.forEach(personalization::addDynamicTemplateData);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);
        mail.addPersonalization(personalization);

        return mail;
    }

    void sendMailToSendGrid(Mail mail, String mailType) {
        log.info("[sendMailToSendGrid] type={}", mailType);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            log.info(
                    "[sendMailToSendGrid] Sent {} mail successfully: status={}, body={}",
                    mailType,
                    response.getStatusCode(),
                    response.getBody());

        } catch (IOException e) {
            log.error(
                    "[sendMailToSendGrid] Failed to send {} mail: {}", mailType, e.getMessage(), e);
        }
    }

    String buildGroupTransactionUrl(UUID groupId, UUID transactionId) {
        return BASE_URL + "/groups/" + groupId + "/transactions/" + transactionId;
    }
}
