package backend.academy.linktracker.bot.repository.orm;

import backend.academy.linktracker.bot.repository.BotRepository;
import backend.academy.linktracker.bot.repository.BotState;
import backend.academy.linktracker.commondto.entity.BotUser;
import backend.academy.linktracker.commondto.entity.BotUserSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class OrmBotRepository implements BotRepository {
    private final OrmBotUserRepository botUserRepository;
    private final OrmBotUserSessionRepository botUserSessionRepository;

    @Override
    @Transactional
    public void addChat(long chatId) {
        botUserRepository.findByChatId(chatId).orElseGet(() -> {
            BotUser user = new BotUser();
            user.setChatId(chatId);
            return botUserRepository.save(user);
        });
    }

    @Override
    public boolean isPresent(long chatId) {
        return botUserRepository.existsByChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public BotState getState(long chatId) {
        return botUserSessionRepository
                .findById(chatId)
                .map(s -> BotState.valueOf(s.getChatState()))
                .orElse(BotState.AWAITING_COMMAND);
    }

    @Override
    @Transactional
    public void setState(long chatId, BotState state) {
        botUserRepository.findByChatId(chatId).orElseGet(() -> {
            BotUser newUser = new BotUser();
            newUser.setChatId(chatId);
            return botUserRepository.save(newUser);
        });

        BotUserSession session = botUserSessionRepository.findById(chatId).orElseGet(() -> new BotUserSession(chatId));

        session.setChatState(state.name());
        botUserSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getArgs(long chatId) {
        return botUserSessionRepository
                .findById(chatId)
                .map(BotUserSession::getArguments)
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void setArgs(long chatId, List<String> args) {
        BotUserSession session = botUserSessionRepository
                .findById(chatId)
                .orElseThrow(() -> new RuntimeException("Session for chatId " + chatId + " not found"));
        session.setArguments(args);
        botUserSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getTags(long chatId) {
        return botUserSessionRepository
                .findById(chatId)
                .map(BotUserSession::getTags)
                .orElse(List.of());
    }

    @Override
    @Transactional
    public void setTags(long chatId, List<String> tags) {
        BotUserSession session = botUserSessionRepository
                .findById(chatId)
                .orElseThrow(() -> new RuntimeException("Session for chatId " + chatId + " not found"));
        session.setTags(tags);
        botUserSessionRepository.save(session);
    }
}
