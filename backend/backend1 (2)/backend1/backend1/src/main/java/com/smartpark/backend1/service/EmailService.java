package com.smartpark.backend1.service;

import com.smartpark.backend1.model.Evenement;
import com.smartpark.backend1.model.Inscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${smartpark.mail.from}")
    private String fromEmail;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── 1. Email : Ajout en liste d'attente ───────────────────
    @Async
    public void envoyerEmailListeAttente(Inscription inscription, Evenement evenement) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(inscription.getParticipantEmail());
            helper.setSubject("⏳ Vous êtes sur liste d'attente — " + evenement.getTitre());
            helper.setText(buildEmailListeAttente(inscription, evenement), true);

            mailSender.send(message);
            System.out.println("✅ Email liste d'attente envoyé à : " + inscription.getParticipantEmail());

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email liste d'attente : " + e.getMessage());
            // On ne lance pas d'exception pour ne pas bloquer l'inscription
        }
    }

    // ── 2. Email : Promotion — place confirmée ─────────────────
    @Async
    public void envoyerEmailPromotion(Inscription inscription, Evenement evenement) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(inscription.getParticipantEmail());
            helper.setSubject("🎉 Votre place est confirmée — " + evenement.getTitre());
            helper.setText(buildEmailPromotion(inscription, evenement), true);

            mailSender.send(message);
            System.out.println("✅ Email promotion envoyé à : " + inscription.getParticipantEmail());

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email promotion : " + e.getMessage());
        }
    }

    // ── 3. Email : Inscription confirmée (normale) ─────────────
    @Async
    public void envoyerEmailConfirmation(Inscription inscription, Evenement evenement) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(inscription.getParticipantEmail());
            helper.setSubject("✅ Inscription confirmée — " + evenement.getTitre());
            helper.setText(buildEmailConfirmation(inscription, evenement), true);

            mailSender.send(message);
            System.out.println("✅ Email confirmation envoyé à : " + inscription.getParticipantEmail());

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi email confirmation : " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════
    // TEMPLATES HTML DES EMAILS
    // ══════════════════════════════════════════════════════════

    private String buildEmailListeAttente(Inscription ins, Evenement ev) {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
          <meta charset="UTF-8">
          <style>
            body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f4f8; margin: 0; padding: 0; }
            .container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
            .header { background: linear-gradient(135deg, #0d1117, #1a2030); padding: 40px 32px; text-align: center; }
            .logo { font-size: 2rem; margin-bottom: 8px; }
            .brand { font-size: 1.5rem; font-weight: 800; color: #fff; letter-spacing: -0.5px; }
            .brand span { color: #00d97e; }
            .header-sub { font-size: 0.8rem; color: rgba(255,255,255,0.4); margin-top: 4px; text-transform: uppercase; letter-spacing: 1px; }
            .body { padding: 40px 32px; }
            .greeting { font-size: 1.1rem; color: #1e293b; margin-bottom: 16px; }
            .waitlist-box { background: linear-gradient(135deg, #fef9c3, #fef3c7); border: 2px solid #fde68a; border-radius: 16px; padding: 28px; text-align: center; margin: 24px 0; }
            .waitlist-emoji { font-size: 3rem; display: block; margin-bottom: 12px; }
            .waitlist-title { font-size: 1.25rem; font-weight: 800; color: #92400e; margin-bottom: 8px; }
            .waitlist-pos { font-size: 3rem; font-weight: 800; color: #d97706; line-height: 1; margin: 12px 0; }
            .waitlist-pos-label { font-size: 0.85rem; color: #78350f; }
            .event-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px 24px; margin: 24px 0; }
            .event-title { font-size: 1.1rem; font-weight: 700; color: #0f172a; margin-bottom: 12px; }
            .event-detail { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem; }
            .event-detail:last-child { border-bottom: none; }
            .detail-label { color: #64748b; }
            .detail-val { font-weight: 600; color: #1e293b; }
            .info-list { margin: 24px 0; }
            .info-item { display: flex; align-items: flex-start; gap: 12px; padding: 10px 0; font-size: 0.9rem; color: #374151; border-bottom: 1px solid #f1f5f9; }
            .info-item:last-child { border-bottom: none; }
            .info-icon { font-size: 1.1rem; flex-shrink: 0; margin-top: 2px; }
            .ref-box { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 10px; padding: 14px 20px; text-align: center; margin: 20px 0; }
            .ref-label { font-size: 0.72rem; text-transform: uppercase; letter-spacing: .08em; color: #3b82f6; font-weight: 700; margin-bottom: 4px; }
            .ref-val { font-family: 'Courier New', monospace; font-size: 1.1rem; font-weight: 800; color: #1d4ed8; letter-spacing: .1em; }
            .footer { background: #f8fafc; padding: 24px 32px; text-align: center; font-size: 0.8rem; color: #94a3b8; border-top: 1px solid #e2e8f0; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <div class="logo">🏟️</div>
              <div class="brand">Smart<span>Park</span></div>
              <div class="header-sub">Gestion des Événements</div>
            </div>
            <div class="body">
              <p class="greeting">Bonjour <strong>%s</strong>,</p>
              <p style="color:#374151;font-size:0.95rem;line-height:1.6;">
                L'événement <strong>%s</strong> est complet. Cependant, vous avez bien été enregistré(e) sur la <strong>liste d'attente</strong>.
              </p>

              <div class="waitlist-box">
                <span class="waitlist-emoji">⏳</span>
                <div class="waitlist-title">Vous êtes en liste d'attente</div>
                <div class="waitlist-pos">#%d</div>
                <div class="waitlist-pos-label">Votre position dans la file</div>
              </div>

              <div class="event-box">
                <div class="event-title">📅 %s</div>
                <div class="event-detail">
                  <span class="detail-label">📍 Lieu</span>
                  <span class="detail-val">%s</span>
                </div>
                <div class="event-detail">
                  <span class="detail-label">📅 Date</span>
                  <span class="detail-val">%s</span>
                </div>
                <div class="event-detail">
                  <span class="detail-label">⏰ Horaire</span>
                  <span class="detail-val">%s — %s</span>
                </div>
              </div>

              <div class="info-list">
                <div class="info-item">
                  <span class="info-icon">✅</span>
                  <span>Si quelqu'un annule, vous serez <strong>automatiquement confirmé(e)</strong> selon votre position.</span>
                </div>
                <div class="info-item">
                  <span class="info-icon">📧</span>
                  <span>Vous recevrez un email immédiatement dès que votre place est disponible.</span>
                </div>
                <div class="info-item">
                  <span class="info-icon">💳</span>
                  <span>Le paiement ne sera demandé qu'après confirmation de votre place.</span>
                </div>
                <div class="info-item">
                  <span class="info-icon">❌</span>
                  <span>Si vous ne souhaitez plus attendre, contactez-nous pour retirer votre inscription.</span>
                </div>
              </div>

              <div class="ref-box">
                <div class="ref-label">N° de référence</div>
                <div class="ref-val">%s</div>
              </div>

              <p style="color:#64748b;font-size:0.85rem;margin-top:24px;">
                Conservez cet email comme preuve de votre inscription en liste d'attente.
              </p>
            </div>
            <div class="footer">
              <p>© 2025 SmartPark · Tous droits réservés</p>
              <p style="margin-top:6px;">Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(
                ins.getParticipantNom(),
                ev.getTitre(),
                ins.getPositionAttente(),
                ev.getTitre(),
                ev.getLieu(),
                ev.getDateDebut().format(DATE_FORMAT),
                ev.getHeureDebut(),
                ev.getHeureFin(),
                ins.getNumeroBillet()
        );
    }

    private String buildEmailPromotion(Inscription ins, Evenement ev) {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
          <meta charset="UTF-8">
          <style>
            body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f4f8; margin: 0; padding: 0; }
            .container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
            .header { background: linear-gradient(135deg, #065f46, #059669); padding: 40px 32px; text-align: center; }
            .logo { font-size: 2rem; margin-bottom: 8px; }
            .brand { font-size: 1.5rem; font-weight: 800; color: #fff; }
            .brand span { color: #a7f3d0; }
            .header-sub { font-size: 0.8rem; color: rgba(255,255,255,0.6); margin-top: 4px; text-transform: uppercase; letter-spacing: 1px; }
            .body { padding: 40px 32px; }
            .confirm-box { background: linear-gradient(135deg, #dcfce7, #bbf7d0); border: 2px solid #86efac; border-radius: 16px; padding: 28px; text-align: center; margin: 24px 0; }
            .confirm-emoji { font-size: 3.5rem; display: block; margin-bottom: 8px; }
            .confirm-title { font-size: 1.3rem; font-weight: 800; color: #065f46; }
            .confirm-sub   { font-size: 0.9rem; color: #047857; margin-top: 8px; }
            .billet-box { background: linear-gradient(135deg, #1e293b, #334155); border-radius: 14px; padding: 20px 24px; margin: 24px 0; }
            .billet-num-label { font-size: 0.7rem; text-transform: uppercase; letter-spacing: .08em; color: rgba(255,255,255,0.4); margin-bottom: 4px; }
            .billet-num { font-family: 'Courier New', monospace; font-size: 1.2rem; font-weight: 800; color: #a7f3d0; letter-spacing: .1em; }
            .event-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px 24px; margin: 20px 0; }
            .event-title { font-size: 1.1rem; font-weight: 700; color: #0f172a; margin-bottom: 12px; }
            .event-detail { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem; }
            .event-detail:last-child { border-bottom: none; }
            .detail-label { color: #64748b; }
            .detail-val { font-weight: 600; color: #1e293b; }
            .price-box { background: #fffbeb; border: 2px solid #fde68a; border-radius: 10px; padding: 14px 20px; display: flex; justify-content: space-between; align-items: center; margin: 20px 0; }
            .price-label { font-size: 0.88rem; color: #92400e; font-weight: 600; }
            .price-val { font-size: 1.4rem; font-weight: 800; color: #d97706; }
            .footer { background: #f8fafc; padding: 24px 32px; text-align: center; font-size: 0.8rem; color: #94a3b8; border-top: 1px solid #e2e8f0; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <div class="logo">🏟️</div>
              <div class="brand">Smart<span>Park</span></div>
              <div class="header-sub">Confirmation de place</div>
            </div>
            <div class="body">
              <p style="font-size:1.1rem;color:#1e293b;margin-bottom:16px;">Bonjour <strong>%s</strong>,</p>
              <p style="color:#374151;font-size:0.95rem;line-height:1.6;">
                Bonne nouvelle ! Une place s'est libérée pour l'événement <strong>%s</strong>.
                Votre inscription a été <strong>automatiquement confirmée</strong>.
              </p>

              <div class="confirm-box">
                <span class="confirm-emoji">🎉</span>
                <div class="confirm-title">Votre place est confirmée !</div>
                <div class="confirm-sub">Vous étiez en liste d'attente — votre tour est arrivé.</div>
              </div>

              <div class="billet-box">
                <div class="billet-num-label">N° de billet</div>
                <div class="billet-num">%s</div>
              </div>

              <div class="event-box">
                <div class="event-title">📅 %s</div>
                <div class="event-detail">
                  <span class="detail-label">📍 Lieu</span><span class="detail-val">%s</span>
                </div>
                <div class="event-detail">
                  <span class="detail-label">📅 Date</span><span class="detail-val">%s</span>
                </div>
                <div class="event-detail">
                  <span class="detail-label">⏰ Horaire</span><span class="detail-val">%s — %s</span>
                </div>
              </div>

              <div class="price-box">
                <span class="price-label">Montant à régler</span>
                <span class="price-val">%s DT</span>
              </div>

              <p style="color:#64748b;font-size:0.85rem;line-height:1.5;">
                Présentez ce billet à l'entrée de l'événement.<br>
                Ce billet est personnel et non-transférable.
              </p>
            </div>
            <div class="footer">
              <p>© 2025 SmartPark · Tous droits réservés</p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(
                ins.getParticipantNom(),
                ev.getTitre(),
                ins.getNumeroBillet(),
                ev.getTitre(),
                ev.getLieu(),
                ev.getDateDebut().format(DATE_FORMAT),
                ev.getHeureDebut(),
                ev.getHeureFin(),
                String.format("%.2f", ins.getMontantPaye())
        );
    }

    private String buildEmailConfirmation(Inscription ins, Evenement ev) {
        return """
        <!DOCTYPE html>
        <html lang="fr">
        <head>
          <meta charset="UTF-8">
          <style>
            body { font-family: 'Segoe UI', Arial, sans-serif; background: #f0f4f8; margin: 0; padding: 0; }
            .container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 24px rgba(0,0,0,0.08); }
            .header { background: linear-gradient(135deg, #0d1117, #1a2030); padding: 40px 32px; text-align: center; }
            .logo { font-size: 2rem; margin-bottom: 8px; }
            .brand { font-size: 1.5rem; font-weight: 800; color: #fff; }
            .brand span { color: #00d97e; }
            .body { padding: 40px 32px; }
            .success-box { background: linear-gradient(135deg, #dcfce7, #bbf7d0); border: 2px solid #86efac; border-radius: 16px; padding: 24px; text-align: center; margin: 24px 0; }
            .billet-preview { background: linear-gradient(135deg, #1e293b, #334155); border-radius: 14px; padding: 20px 24px; margin: 20px 0; }
            .billet-num-label { font-size: 0.7rem; text-transform: uppercase; letter-spacing: .08em; color: rgba(255,255,255,0.4); }
            .billet-num { font-family: 'Courier New', monospace; font-size: 1.2rem; font-weight: 800; color: #a7f3d0; letter-spacing: .1em; margin-top: 4px; }
            .event-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px 24px; margin: 20px 0; }
            .event-detail { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #f1f5f9; font-size: 0.9rem; }
            .event-detail:last-child { border-bottom: none; }
            .detail-label { color: #64748b; }
            .detail-val { font-weight: 600; color: #1e293b; }
            .footer { background: #f8fafc; padding: 24px 32px; text-align: center; font-size: 0.8rem; color: #94a3b8; border-top: 1px solid #e2e8f0; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <div class="logo">🏟️</div>
              <div class="brand">Smart<span>Park</span></div>
            </div>
            <div class="body">
              <p style="font-size:1.1rem;color:#1e293b;">Bonjour <strong>%s</strong>,</p>
              <div class="success-box">
                <div style="font-size:2.5rem;margin-bottom:8px;">🎉</div>
                <div style="font-size:1.2rem;font-weight:800;color:#065f46;">Inscription confirmée !</div>
                <div style="font-size:0.9rem;color:#047857;margin-top:6px;">Votre place est réservée pour <strong>%s</strong></div>
              </div>
              <div class="billet-preview">
                <div class="billet-num-label">N° de billet</div>
                <div class="billet-num">%s</div>
              </div>
              <div class="event-box">
                <div class="event-detail"><span class="detail-label">📍 Lieu</span><span class="detail-val">%s</span></div>
                <div class="event-detail"><span class="detail-label">📅 Date</span><span class="detail-val">%s</span></div>
                <div class="event-detail"><span class="detail-label">⏰ Horaire</span><span class="detail-val">%s — %s</span></div>
                <div class="event-detail"><span class="detail-label">💰 Montant</span><span class="detail-val">%s DT</span></div>
              </div>
            </div>
            <div class="footer"><p>© 2025 SmartPark · Tous droits réservés</p></div>
          </div>
        </body>
        </html>
        """.formatted(
                ins.getParticipantNom(),
                ev.getTitre(),
                ins.getNumeroBillet(),
                ev.getLieu(),
                ev.getDateDebut().format(DATE_FORMAT),
                ev.getHeureDebut(),
                ev.getHeureFin(),
                String.format("%.2f", ins.getMontantPaye())
        );
    }
}