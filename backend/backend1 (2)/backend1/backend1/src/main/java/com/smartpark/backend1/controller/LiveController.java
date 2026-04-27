package com.smartpark.backend1.controller;

import com.smartpark.backend1.service.LiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
public class LiveController {

  @Autowired
  private LiveService liveService;

  /**
   * Message STOMP envoyé par Angular quand un user arrive sur la page.
   * Le client envoie vers : /app/live/{evenementId}/join
   */
  @MessageMapping("/live/{evenementId}/join")
  public void joinPage(@DestinationVariable String evenementId) {
    liveService.visiteurRejoint(evenementId);
  }

  /**
   * Message STOMP envoyé par Angular quand un user quitte la page.
   * Le client envoie vers : /app/live/{evenementId}/leave
   */
  @MessageMapping("/live/{evenementId}/leave")
  public void leavePage(@DestinationVariable String evenementId) {
    liveService.visiteurPart(evenementId);
  }

  /**
   * REST : retourne le nombre de visiteurs courants (utile pour debug)
   */
  @GetMapping("/api/live/{evenementId}/visiteurs")
  public int getVisiteurs(@PathVariable String evenementId) {
    return liveService.getVisiteurs(evenementId);
  }
}
