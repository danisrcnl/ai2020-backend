package it.polito.ai.lab2.services.notification;
import it.polito.ai.lab2.controllers.NotificationController;
import it.polito.ai.lab2.dataStructures.MemberStatus;
import it.polito.ai.lab2.dtos.StudentDTO;
import it.polito.ai.lab2.entities.TeamNotFoundException;
import it.polito.ai.lab2.entities.Token;
import it.polito.ai.lab2.entities.User;
import it.polito.ai.lab2.entities.UserNotFoundException;
import it.polito.ai.lab2.repositories.TokenRepository;
import it.polito.ai.lab2.repositories.UserRepository;
import it.polito.ai.lab2.services.team.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Transactional
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    TeamService teamService;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public void sendMessage(String address, String subject, String body) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo("kribos3@hotmail.it");
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(body);
        javaMailSender.send(simpleMailMessage);
    }

    @Override
    public boolean confirm(String token) {

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Europe/Paris"));

        if(!tokenRepository.existsById(token))
            return false;

        Token t = tokenRepository.getOne(token);
        int teamId = t.getTeamId();
        List<Token> liveTokens = tokenRepository.findAllByExpiryBefore(Timestamp.valueOf(localDateTime));

        if(!liveTokens.contains(t)) {
            List<Token> teamTokens = tokenRepository.findAllByTeamId(teamId);
            for(Token teamToken : teamTokens)
                tokenRepository.delete(teamToken);
            tokenRepository.flush();
            teamService.evictTeamById(teamId);
            return false;
        }

        tokenRepository.delete(t);
        tokenRepository.flush();

        if(tokenRepository.findAllByTeamId(teamId).isEmpty())
            teamService.activateTeamById(teamId);

        return true;
    }

    @Override
    public boolean reject(String token) {

        if(!tokenRepository.existsById(token))
            return false;

        Token t = tokenRepository.getOne(token);
        int teamId = t.getTeamId();
        List<Token> teamTokens = tokenRepository.findAllByTeamId(teamId);
        for(Token teamToken : teamTokens)
            tokenRepository.delete(teamToken);
        tokenRepository.flush();
        teamService.evictTeamById(teamId);
        return true;
    }

    @Override
    public void notifyTeam (String courseName, String teamName, List<String> memberIds, int hours) {

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Europe/Paris")).plusHours(hours);

        Token t = Token.builder()
                .teamId(teamService.getTeamId(courseName, teamName))
                .expiryDate(Timestamp.valueOf(localDateTime))
                .isTeam(true)
                .build();

        for(String memberId : memberIds) {
            String id = UUID.randomUUID().toString();
            t.setId(id);
            t.setStudentId(memberId);
            tokenRepository.save(t);
            tokenRepository.flush();
            Link rootLink = linkTo(NotificationController.class).withSelfRel();
            String confirmLink = rootLink.getHref() + "/confirm/" + id;
            String rejectLink = rootLink.getHref() + "/reject/" + id;
            String message = "Ciao s" + memberId + "@studenti.polito.it, questo è un messaggio generato per gli " +
                    "utenti del team " + teamName + "!" + System.lineSeparator() +
                    "Clicca qui per confermare l'invito:" + System.lineSeparator()
                    + confirmLink + System.lineSeparator() + "Clicca qui per rifiutare l'invito:" +
                    System.lineSeparator() + rejectLink;
            String receiver = "s" + memberId + "@studenti.polito.it";
            String subject = "[VirtualLabs] Sei stato invitato a far parte di un team!";
            sendMessage(receiver, subject, message);
        }
    }

    @Override
    public void notifyUser (String email) throws UserNotFoundException {

        if(!userRepository.findByUsername(email).isPresent())
            throw new UserNotFoundException(email);
        User u = userRepository.findByUsername(email).get();
        Long userId = u.getId();

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Europe/Paris")).plusHours(24);

        String id = UUID.randomUUID().toString();
        Token t = Token.builder()
                .id(id)
                .userId(userId)
                .expiryDate(Timestamp.valueOf(localDateTime))
                .isTeam(false)
                .build();
        tokenRepository.save(t);
        tokenRepository.flush();

        Link rootLink = linkTo(NotificationController.class).withSelfRel();
        String confirmLink = rootLink.getHref() + "/register/confirm/" + id;
        String message = "Ciao " + u.getUsername() + "! Grazie per esserti iscritto, trovi qui sotto il link " +
                " per confermare la tua iscrizione:" + System.lineSeparator() + confirmLink + System.lineSeparator() +
                System.lineSeparator() + "Se non sei stato tu a effettuare questa operazione, ti invitiamo ad ignorare la mail.";
        String receiver = u.getUsername();
        String subject = "[VirtualLabs] Conferma registrazione";
        sendMessage(receiver, subject, message);
    }

    @Override
    public boolean confirmUser(String token) throws UserNotFoundException {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Europe/Paris"));

        if(!tokenRepository.existsById(token))
            return false;

        Token t = tokenRepository.getOne(token);
        List<Token> liveTokens = tokenRepository.findAllByExpiryBefore(Timestamp.valueOf(localDateTime));
        if(!liveTokens.contains(t))
            return false;
        Long userId = t.getUserId();
        if(!userRepository.existsById(userId))
            throw new UserNotFoundException(userId.toString());
        userRepository.getOne(userId).setActive(true);
        return true;
    }

    @Override
    public List<MemberStatus> getMembersStatus(int teamId) throws TeamNotFoundException {
        List<String> membersIds =
                teamService
                        .getMembersById(teamId)
                        .stream()
                        .map(StudentDTO::getId)
                        .collect(Collectors.toList());
        List<String> pending =
                tokenRepository
                        .findAllByTeamId(teamId)
                        .stream()
                        .map(Token::getStudentId)
                        .collect(Collectors.toList());

        List<MemberStatus> memberStatuses = new ArrayList<>();

        for(String member : membersIds) {
            MemberStatus memberStatus = MemberStatus.builder().studentId(member).build();
            memberStatus.setHasAccepted(!pending.contains(member));
            memberStatuses.add(memberStatus);
        }
        return memberStatuses;
    }
}