package com.taobao.arthas.core.command;

import com.taobao.arthas.core.bytecode.TestHelper;
import com.taobao.arthas.core.command.model.ResultModel;
import com.taobao.arthas.core.server.ArthasBootstrap;
import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.session.SessionManager;

import net.bytebuddy.agent.ByteBuddyAgent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;



/**
 * 针对 CommandExecutorImpl 的 executeSync 方法进行测试，重点覆盖 retransfrom 命令
 */
class CommandExecutorTest {

    private SessionManager sessionManager;
    private CommandExecutorImpl commandExecutor;

    @BeforeEach
    void setUp() throws Throwable {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        TestHelper.appendSpyJar(instrumentation);

        ArthasBootstrap instance = ArthasBootstrap.getInstance(instrumentation, "ip=127.0.0.1");
        sessionManager = instance.getSessionManager();
        commandExecutor = new CommandExecutorImpl(sessionManager);
    }

    @Test
    void testExecuteSyncRetransformCommand_Success() {
        // 构造 retransfrom 命令
        String commandLine = "retransform -l";
        long timeout = 500000L;

        // 执行命令
        Map<String, Object> result = commandExecutor.executeSync(commandLine, timeout, null, null, null);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.get("success")).isEqualTo(true);
        assertThat(result.get("command")).isEqualTo(commandLine);
        assertThat(result.get("sessionId")).isNotNull();

        // 进一步检查 results 内容（如有）
        if (result.get("results") instanceof List<?>) {
            List<?> results = (List<?>) result.get("results");
            // 结果可能为空或包含 ResultModel，具体断言可根据实际实现调整
            assertThat(results).allMatch(r -> r instanceof ResultModel);
        }
    }

    // 工具类：用于生成 mock Session
    static class TestSessionFactory {
        static com.taobao.arthas.core.shell.session.Session createMockSession() {
            com.taobao.arthas.core.shell.session.Session session = mock(com.taobao.arthas.core.shell.session.Session.class, RETURNS_DEEP_STUBS);
            when(session.getSessionId()).thenReturn("test-session-id");
            return session;
        }
    }
}