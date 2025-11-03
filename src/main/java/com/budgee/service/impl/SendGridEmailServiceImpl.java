package com.budgee.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.budgee.service.EmailService;
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

    SendGrid sendGrid;

    @NonFinal
    @Value("${spring.mail.from}")
    String MAIL_FROM;

    @NonFinal
    @Value("${spring.sendgrid.register-template}")
    String REGISTER_TEMPLATE_EMAIL;

    @Override
    public void sendRegisterEmail(String toEmail, String fullName, String verificationLink) {
        log.info("[sendRegisterEmail] toEmail={}", toEmail);

        Email from = new Email(MAIL_FROM);
        Email to = new Email(toEmail);
        Mail mail = new Mail();

        mail.setFrom(from);
        mail.setTemplateId(REGISTER_TEMPLATE_EMAIL);
        mail.setSubject("Welcome to Budgee!");

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("full_name", fullName);
        personalization.addDynamicTemplateData("verification_link", verificationLink);

        mail.addPersonalization(personalization);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            log.info("[sendRegisterEmail] send mail successfully={}", response.toString());
        } catch (IOException e) {
            log.error("[sendRegisterEmail] error at send mail = {}", e.getMessage());
        }
    }
}
