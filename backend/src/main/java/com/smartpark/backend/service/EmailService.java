package com.smartpark.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

        @Autowired(required = false)
    private JavaMailSender mailSender;

        @Value("${smartpark.mail.from:noreply@smartpark.com}")
    private String fromAddress;

    // ✅ Méthode générique d'envoi HTML
    @Async
    public void envoyerEmail(
            String to,
            String subject,
            String htmlContent) {
        try {
                        if (mailSender == null) {
                                System.err.println("⚠️ JavaMailSender indisponible: email non envoyé vers " + to);
                                return;
                        }

            MimeMessage message =
                    mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println(
                    "✅ Email envoyé à : " + to
                            + " | Sujet : " + subject);

        } catch (Exception e) {
            System.err.println(
                    "❌ Erreur email vers "
                            + to + " : " + e.getMessage());
        }
    }

    // ════════════════════════════════════════════
    // 📩 EMAIL 1 — Confirmation inscription match
    // ════════════════════════════════════════════
    @Async
    public void envoyerConfirmationInscription(
            String email,
            String nomJoueur,
            String titreMatch,
            String sport,
            String date,
            String heure,
            String terrainNom,
            String format) {

        String html = buildEmailTemplate(
                "✅ Inscription confirmée !",
                "Votre match vous attend",
                nomJoueur,
                buildConfirmationBody(
                        titreMatch, sport, date,
                        heure, terrainNom, format),
                "var(--green, #00C97F)",
                "⚽"
        );

        envoyerEmail(
                email,
                "✅ Confirmation match — " + titreMatch,
                html);
    }

    // ════════════════════════════════════════════
    // 📩 EMAIL 2 — Match complet
    // ════════════════════════════════════════════
    @Async
    public void envoyerMatchComplet(
            String email,
            String nomJoueur,
            String titreMatch,
            String sport,
            String date,
            String heure,
            int nbJoueurs) {

        String html = buildEmailTemplate(
                "🔥 Match complet !",
                "Tous les joueurs sont inscrits",
                nomJoueur,
                buildMatchCompletBody(
                        titreMatch, sport, date,
                        heure, nbJoueurs),
                "#F5A623",
                "🏟️"
        );

        envoyerEmail(
                email,
                "🔥 Match complet — " + titreMatch,
                html);
    }

    // ════════════════════════════════════════════
    // 📩 EMAIL 3 — Place disponible (liste attente)
    // ════════════════════════════════════════════
    @Async
    public void envoyerPlaceDisponible(
            String email,
            String nomJoueur,
            String titreMatch,
            String sport,
            String date,
            String heure,
            int position) {

        String html = buildEmailTemplate(
                "⚡ Une place se libère !",
                "Dépêchez-vous de rejoindre",
                nomJoueur,
                buildPlaceDisponibleBody(
                        titreMatch, sport, date,
                        heure, position),
                "#3B9EFF",
                "🎉"
        );

        envoyerEmail(
                email,
                "⚡ Place disponible — " + titreMatch,
                html);
    }

    // ════════════════════════════════════════════
    // 📩 EMAIL 4 — Inscription liste d'attente
    // ════════════════════════════════════════════
    @Async
    public void envoyerInscriptionAttente(
            String email,
            String nomJoueur,
            String titreMatch,
            String sport,
            String date,
            String heure,
            int position) {

        String html = buildEmailTemplate(
                "⏳ Vous êtes en liste d'attente",
                "Votre position est réservée",
                nomJoueur,
                buildAttenteBody(
                        titreMatch, sport, date,
                        heure, position),
                "#A78BFA",
                "⏳"
        );

        envoyerEmail(
                email,
                "⏳ Liste d'attente — " + titreMatch,
                html);
    }

    // ════════════════════════════════════════════
    // 📩 EMAIL 5 — Match annulé
    // ════════════════════════════════════════════
    @Async
    public void envoyerMatchAnnule(
            String email,
            String nomJoueur,
            String titreMatch,
            String sport,
            String date,
            String heure) {

        String html = buildEmailTemplate(
                "❌ Match annulé",
                "Nous sommes désolés",
                nomJoueur,
                buildMatchAnnuleBody(
                        titreMatch, sport, date, heure),
                "#FF4D6A",
                "😞"
        );

        envoyerEmail(
                email,
                "❌ Match annulé — " + titreMatch,
                html);
    }

    // ════════════════════════════════════════════
    // 📩 EMAIL 6 — Confirmation réservation terrain
    // ════════════════════════════════════════════
    @Async
    public void envoyerConfirmationReservation(
            String email,
            String nomClient,
            String terrainNom,
            String date,
            String heureDebut,
            String heureFin,
            double montant) {

        String html = buildEmailTemplate(
                "✅ Réservation confirmée !",
                "Votre terrain vous attend",
                nomClient,
                buildReservationBody(
                        terrainNom, date,
                        heureDebut, heureFin, montant),
                "#00C97F",
                "🏟️"
        );

        envoyerEmail(
                email,
                "✅ Réservation confirmée — " + terrainNom,
                html);
    }

    // ════════════════════════════════════════════
    // 🎨 TEMPLATE HTML PRINCIPAL
    // ════════════════════════════════════════════
    private String buildEmailTemplate(
            String titre,
            String sousTitre,
            String nomDestinataire,
            String contenu,
            String couleur,
            String emoji) {

        return "<!DOCTYPE html>"
                + "<html lang='fr'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' "
                + "content='width=device-width'>"
                + "<title>SmartPark</title>"
                + "</head>"
                + "<body style='"
                + "margin:0;padding:0;"
                + "background:#0A0F14;"
                + "font-family:Arial,sans-serif;'>"

                // Wrapper
                + "<div style='"
                + "max-width:600px;margin:0 auto;"
                + "padding:24px 16px;'>"

                // HEADER
                + "<div style='"
                + "background:#141C26;"
                + "border-radius:20px 20px 0 0;"
                + "padding:32px;text-align:center;"
                + "border:1px solid #1E2A38;"
                + "border-bottom:none;'>"

                + "<div style='"
                + "font-size:48px;margin-bottom:12px;"
                + "'>" + emoji + "</div>"

                + "<div style='"
                + "display:inline-flex;"
                + "align-items:center;gap:8px;"
                + "font-size:22px;font-weight:800;"
                + "color:#F0F4F8;margin-bottom:8px;'>"
                + "⚽ SmartPark"
                + "</div>"

                + "<div style='"
                + "font-size:26px;font-weight:800;"
                + "color:" + couleur + ";"
                + "margin-bottom:6px;'>"
                + titre
                + "</div>"

                + "<div style='"
                + "font-size:15px;color:#8A9BB0;'>"
                + sousTitre
                + "</div>"
                + "</div>"

                // BODY
                + "<div style='"
                + "background:#141C26;padding:32px;"
                + "border:1px solid #1E2A38;"
                + "border-top:3px solid " + couleur + ";'>"

                + "<p style='"
                + "color:#F0F4F8;font-size:16px;"
                + "margin:0 0 24px;'>"
                + "Bonjour <strong style='color:"
                + couleur + ";'>"
                + nomDestinataire + "</strong> 👋"
                + "</p>"

                + contenu

                + "</div>"

                // FOOTER
                + "<div style='"
                + "background:#0D1520;padding:24px;"
                + "border-radius:0 0 20px 20px;"
                + "text-align:center;"
                + "border:1px solid #1E2A38;"
                + "border-top:none;'>"

                + "<p style='"
                + "color:#8A9BB0;font-size:13px;"
                + "margin:0 0 8px;'>"
                + "⚽ SmartPark — Plateforme de réservation"
                + " de terrains sportifs"
                + "</p>"

                + "<p style='"
                + "color:#445566;font-size:11px;margin:0;'>"
                + "Cet email a été envoyé automatiquement."
                + " Merci de ne pas y répondre."
                + "</p>"
                + "</div>"

                + "</div>"
                + "</body></html>";
    }

    // ════════════════════════════════════════════
    // 📦 BUILDERS CORPS DES EMAILS
    // ════════════════════════════════════════════

    private String buildConfirmationBody(
            String titreMatch, String sport,
            String date, String heure,
            String terrainNom, String format) {

        return infoCard(
                "Votre inscription est confirmée ! "
                        + "Préparez-vous pour un super match 🔥")

                + detailsCard(new String[][]{
                {"⚽ Match",   titreMatch},
                {"🏅 Sport",   sport},
                {"📋 Format",  format},
                {"📅 Date",    date},
                {"🕐 Heure",   heure},
                {"🏟️ Terrain", terrainNom != null
                        && !terrainNom.isBlank()
                        ? terrainNom : "À confirmer"}
        }, "#00C97F")

                + tipCard(
                "💡 Conseil",
                "Arrivez 10 minutes avant pour "
                        + "vous échauffer et rencontrer "
                        + "vos coéquipiers !",
                "rgba(0,201,127,0.08)",
                "#00C97F");
    }

    private String buildMatchCompletBody(
            String titreMatch, String sport,
            String date, String heure,
            int nbJoueurs) {

        return infoCard(
                "Le match est maintenant COMPLET. "
                        + "Tous vos coéquipiers sont inscrits !")

                + detailsCard(new String[][]{
                {"⚽ Match",    titreMatch},
                {"🏅 Sport",    sport},
                {"📅 Date",     date},
                {"🕐 Heure",    heure},
                {"👥 Joueurs",  nbJoueurs + " inscrits"}
        }, "#F5A623")

                + tipCard(
                "🔥 Préparez-vous !",
                "Votre match approche. Restez hydraté, "
                        + "dormez bien et donnez le meilleur "
                        + "de vous-même !",
                "rgba(245,166,35,0.08)",
                "#F5A623");
    }

    private String buildPlaceDisponibleBody(
            String titreMatch, String sport,
            String date, String heure,
            int position) {

        String posMsg = position == 1
                ? "Vous êtes le PREMIER en liste d'attente "
                + "— cette place est pour VOUS ! 🎯"
                : "Vous êtes le " + position
                + "ème en liste d'attente.";

        return infoCard(
                "🎉 Bonne nouvelle ! Une place vient "
                        + "de se libérer dans votre match. "
                        + posMsg)

                + detailsCard(new String[][]{
                {"⚽ Match",    titreMatch},
                {"🏅 Sport",    sport},
                {"📅 Date",     date},
                {"🕐 Heure",    heure},
                {"📊 Position", "#" + position
                        + " en attente"}
        }, "#3B9EFF")

                + tipCard(
                "⚡ Vite !",
                "Les places partent vite ! "
                        + "Connectez-vous à SmartPark "
                        + "maintenant pour confirmer "
                        + "votre place.",
                "rgba(59,158,255,0.08)",
                "#3B9EFF");
    }

    private String buildAttenteBody(
            String titreMatch, String sport,
            String date, String heure,
            int position) {

        String posMsg;
        if (position == 1)
            posMsg = "🥇 1er en attente — "
                    + "Vous êtes le prochain !";
        else if (position == 2)
            posMsg = "🥈 2ème en attente";
        else if (position == 3)
            posMsg = "🥉 3ème en attente";
        else
            posMsg = "#" + position + " en attente";

        return infoCard(
                "Le match est complet, mais ne vous "
                        + "inquiétez pas ! Votre position est "
                        + "réservée. Vous serez automatiquement "
                        + "inscrit si une place se libère.")

                + detailsCard(new String[][]{
                {"⚽ Match",    titreMatch},
                {"🏅 Sport",    sport},
                {"📅 Date",     date},
                {"🕐 Heure",    heure},
                {"📊 Position", posMsg}
        }, "#A78BFA")

                + tipCard(
                "ℹ️ Comment ça marche ?",
                "Si un joueur se désiste, vous "
                        + "recevrez immédiatement un email "
                        + "et votre place sera confirmée "
                        + "automatiquement !",
                "rgba(167,139,250,0.08)",
                "#A78BFA");
    }

    private String buildMatchAnnuleBody(
            String titreMatch, String sport,
            String date, String heure) {

        return infoCard(
                "Nous sommes désolés de vous informer "
                        + "que ce match a été annulé par "
                        + "l'organisateur.")

                + detailsCard(new String[][]{
                {"⚽ Match",  titreMatch},
                {"🏅 Sport",  sport},
                {"📅 Date",   date},
                {"🕐 Heure",  heure},
                {"📋 Statut", "❌ ANNULÉ"}
        }, "#FF4D6A")

                + tipCard(
                "🔄 Que faire ?",
                "Pas d'inquiétude ! D'autres matchs "
                        + "sont disponibles sur SmartPark. "
                        + "Rejoignez ou créez un nouveau match !",
                "rgba(255,77,106,0.08)",
                "#FF4D6A");
    }

    private String buildReservationBody(
            String terrainNom, String date,
            String heureDebut, String heureFin,
            double montant) {

        return infoCard(
                "Votre réservation est confirmée ! "
                        + "Le terrain vous attend.")

                + detailsCard(new String[][]{
                {"🏟️ Terrain", terrainNom},
                {"📅 Date",    date},
                {"🕐 Début",   heureDebut},
                {"🏁 Fin",     heureFin},
                {"💰 Montant", montant + " DT"}
        }, "#00C97F")

                + tipCard(
                "💡 Rappel",
                "Présentez cet email à l'accueil "
                        + "ou connectez-vous à SmartPark "
                        + "pour voir votre réservation.",
                "rgba(0,201,127,0.08)",
                "#00C97F");
    }

    // ════════════════════════════════════════════
    // 🧱 COMPOSANTS HTML RÉUTILISABLES
    // ════════════════════════════════════════════

    private String infoCard(String message) {
        return "<div style='"
                + "background:#1A2438;border-radius:12px;"
                + "padding:16px 20px;margin-bottom:20px;"
                + "border-left:4px solid #3B9EFF;'>"
                + "<p style='color:#F0F4F8;font-size:15px;"
                + "margin:0;line-height:1.6;'>"
                + message
                + "</p></div>";
    }

    private String detailsCard(
            String[][] rows, String couleur) {

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='"
                + "background:#0D1520;border-radius:12px;"
                + "padding:20px;margin-bottom:20px;"
                + "border:1px solid #1E2A38;'>");

        sb.append("<div style='"
                + "font-size:13px;font-weight:700;"
                + "color:#8A9BB0;text-transform:uppercase;"
                + "letter-spacing:0.06em;margin-bottom:14px;"
                + "'>📋 Détails du match</div>");

        for (String[] row : rows) {
            sb.append("<div style='"
                    + "display:flex;justify-content:"
                    + "space-between;align-items:center;"
                    + "padding:9px 0;"
                    + "border-bottom:1px solid #1E2A38;"
                    + "'>");
            sb.append("<span style='"
                    + "color:#8A9BB0;font-size:14px;'>"
                    + row[0] + "</span>");
            sb.append("<span style='"
                    + "color:#F0F4F8;font-size:14px;"
                    + "font-weight:600;'>"
                    + row[1] + "</span>");
            sb.append("</div>");
        }

        sb.append("</div>");
        return sb.toString();
    }

    // ════════════════════════════════════════════
// 📩 EMAIL 7 — Mise à jour position attente
// ════════════════════════════════════════════
    @Async
    public void envoyerMisAJourPosition(
            String email,
            String nomJoueur,
            String titreMatch,
            String sport,
            String date,
            String heure,
            int nouvellePosition) {

        String posMsg;
        if (nouvellePosition == 1)
            posMsg = "🥇 Vous êtes maintenant PREMIER "
                    + "en liste d'attente !";
        else if (nouvellePosition == 2)
            posMsg = "🥈 Vous êtes maintenant 2ème "
                    + "en liste d'attente.";
        else
            posMsg = "Votre nouvelle position : #"
                    + nouvellePosition;

        String html = buildEmailTemplate(
                "📊 Votre position a changé",
                posMsg,
                nomJoueur,
                buildMajPositionBody(
                        titreMatch, sport,
                        date, heure,
                        nouvellePosition, posMsg),
                "#A78BFA",
                "📊"
        );

        envoyerEmail(
                email,
                "📊 Position liste d'attente — "
                        + titreMatch,
                html);
    }

    private String buildMajPositionBody(
            String titreMatch, String sport,
            String date, String heure,
            int position, String posMsg) {

        return infoCard(
                "Un joueur a quitté la liste d'attente. "
                        + posMsg)

                + detailsCard(new String[][]{
                {"⚽ Match",    titreMatch},
                {"🏅 Sport",    sport},
                {"📅 Date",     date},
                {"🕐 Heure",    heure},
                {"📊 Position", "#" + position
                        + " en attente"}
        }, "#A78BFA")

                + tipCard(
                "ℹ️ Rappel",
                "Vous serez automatiquement inscrit "
                        + "dès qu'une place se libère. "
                        + "Restez connecté sur SmartPark !",
                "rgba(167,139,250,0.08)",
                "#A78BFA");
    }
    private String tipCard(
            String titre, String message,
            String bgColor, String borderColor) {

        return "<div style='"
                + "background:" + bgColor + ";"
                + "border:1px solid "
                + borderColor.replace(")", ",0.3)")
                .replace("rgb", "rgba")
                + ";border-radius:12px;"
                + "padding:16px 20px;margin-bottom:20px;'>"
                + "<div style='font-weight:700;"
                + "color:" + borderColor + ";"
                + "margin-bottom:6px;font-size:14px;'>"
                + titre + "</div>"
                + "<p style='color:#8A9BB0;font-size:13px;"
                + "margin:0;line-height:1.6;'>"
                + message + "</p></div>";
    }
    // ✅ EMAIL — Carte 10 matchs complète
    @Async
    public void envoyerCarteComplete(
            String email, String nom,
            int nbCartes, int points) {

        String html = buildEmailTemplate(
                "🎉 Carte complète !",
                "10 matchs joués — Bravo !",
                nom,
                "<div style='background:#1A2438;"
                        + "border-radius:12px;padding:20px;"
                        + "text-align:center;margin-bottom:20px;"
                        + "border:1px solid #1E2A38;'>"
                        + "<div style='font-size:48px;"
                        + "margin-bottom:12px;'>🎟️</div>"
                        + "<div style='font-size:20px;"
                        + "font-weight:800;color:#00C97F;"
                        + "margin-bottom:8px;'>"
                        + "Vous avez complété votre carte !"
                        + "</div>"
                        + "<div style='color:#8A9BB0;"
                        + "font-size:14px;margin-bottom:16px;'>"
                        + "C'est votre " + nbCartes
                        + "ème carte complétée 🏆"
                        + "</div></div>"
                        + detailsCard(new String[][]{
                        {"🎁 Bonus reçu",    "+50 points"},
                        {"💰 Total points",  points + " pts"},
                        {"🎟️ Cartes",        nbCartes + " carte(s)"},
                        {"🔓 Récompense",    "Réductions exclusives"}
                }, "#00C97F")
                        + tipCard("🎁 Votre récompense !",
                        "Utilisez vos points pour obtenir des "
                                + "réductions sur vos prochaines "
                                + "réservations. 1 point = 0.1 DT !",
                        "rgba(0,201,127,0.08)", "#00C97F"),
                "#00C97F", "🎟️"
        );

        envoyerEmail(email,
                "🎉 Carte 10 matchs complète — SmartPark",
                html);
    }

    // ✅ EMAIL — Montée de niveau
    @Async
    public void envoyerMonteeNiveau(
            String email, String nom,
            String ancienNiveau, String nouveauNiveau,
            double reductionPct) {

        String icon = nouveauNiveau.equals("PLATINUM")
                ? "💎" : nouveauNiveau.equals("GOLD")
                ? "🥇" : "🥈";
        String couleur =
                nouveauNiveau.equals("PLATINUM") ? "#A78BFA"
                        : nouveauNiveau.equals("GOLD")   ? "#F5A623"
                        : "#8A9BB0";

        String html = buildEmailTemplate(
                icon + " Niveau " + nouveauNiveau + " !",
                "Félicitations pour votre progression !",
                nom,
                "<div style='background:#1A2438;"
                        + "border-radius:12px;padding:24px;"
                        + "text-align:center;margin-bottom:20px;"
                        + "border:2px solid " + couleur + ";'>"
                        + "<div style='font-size:56px;"
                        + "margin-bottom:8px;'>" + icon + "</div>"
                        + "<div style='font-size:22px;"
                        + "font-weight:800;color:" + couleur + ";'>"
                        + "Vous êtes maintenant " + nouveauNiveau
                        + " !</div>"
                        + "<div style='color:#8A9BB0;"
                        + "font-size:13px;margin-top:8px;'>"
                        + ancienNiveau + " → " + nouveauNiveau
                        + "</div></div>"
                        + detailsCard(new String[][]{
                        {"📈 Nouveau niveau",   nouveauNiveau},
                        {"💰 Réduction",        reductionPct + "%"},
                        {"🎯 Avantage",
                                "Réduction automatique sur toutes "
                                        + "vos réservations"}
                }, couleur)
                        + tipCard("🚀 Profitez de vos avantages !",
                        "Votre réduction de " + reductionPct
                                + "% est appliquée automatiquement "
                                + "sur toutes vos prochaines réservations.",
                        "rgba(0,201,127,0.05)", couleur),
                couleur, icon
        );

        envoyerEmail(email,
                icon + " Nouveau niveau " + nouveauNiveau
                        + " — SmartPark", html);
    }

    // ✅ EMAIL — Confirmation abonnement
    @Async
    public void envoyerConfirmationAbonnement(
            String email, String nom,
            String type, String dateDebut,
            String dateFin, int matchsTotal,
            double reductionPct, double prix) {

        String icon = type.equals("VIP") ? "💎"
                : type.equals("PREMIUM") ? "🌟" : "⭐";
        String couleur = type.equals("VIP") ? "#A78BFA"
                : type.equals("PREMIUM") ? "#F5A623"
                : "#3B9EFF";

        String html = buildEmailTemplate(
                icon + " Abonnement " + type + " activé !",
                "Bienvenue dans le club SmartPark",
                nom,
                detailsCard(new String[][]{
                        {"📦 Abonnement",    icon + " " + type},
                        {"📅 Début",         dateDebut},
                        {"📅 Fin",           dateFin},
                        {"🎮 Matchs inclus", matchsTotal + " matchs"},
                        {"💰 Réduction",     reductionPct + "%"},
                        {"💳 Prix payé",     prix + " DT/mois"}
                }, couleur)
                        + tipCard("🎁 Vos avantages actifs !",
                        "Votre réduction de " + reductionPct
                                + "% est maintenant active. "
                                + "Vous avez " + matchsTotal
                                + " matchs inclus dans votre abonnement.",
                        "rgba(0,201,127,0.05)", couleur),
                couleur, icon
        );

        envoyerEmail(email,
                icon + " Abonnement " + type
                        + " activé — SmartPark", html);
    }
    // ✅ EMAIL — Points reçus après match terminé
    @Async
    public void envoyerPointsMatchTermine(
            String email,
            String nomJoueur,
            String titreMatch,
            String sport,
            String date,
            String heure,
            int points) {

        String html = buildEmailTemplate(
                "⭐ +" + points + " points gagnés !",
                "Votre match vient de se terminer",
                nomJoueur,
                detailsCard(new String[][]{
                        {"⚽ Match",   titreMatch},
                        {"🏅 Sport",   sport},
                        {"📅 Date",    date},
                        {"🕐 Heure",   heure},
                        {"⭐ Points",  "+" + points
                                + " points fidélité"}
                }, "#00C97F")
                        + tipCard(
                        "🎯 Continuez comme ça !",
                        "Vos points s'accumulent à chaque "
                                + "match joué. Consultez votre carte "
                                + "fidélité sur SmartPark !",
                        "rgba(0,201,127,0.08)",
                        "#00C97F"),
                "#00C97F", "⭐"
        );

        envoyerEmail(
                email,
                "⭐ +" + points
                        + " points — Match terminé SmartPark",
                html);
    }
}