import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    ArrayList<String> fileServerList = new ArrayList<>();       // массив для сохранения списка файлов на сервере
    private String nikName;

    public ServerHandler (String nikName){
        this.nikName = nikName;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Клиент подключился...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof FileMessage) {       //Принимаем файл
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("server_file",nikName, fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                if (Files.exists(Paths.get("server_file", nikName, fm.getFilename()))) {
                    ctx.writeAndFlush(new Message("Файл успешно передан"));
                } else {
                    ctx.writeAndFlush(new Message("Файл не был передан"));
                }
                refreshServerFileList(ctx);
            }
            if(msg instanceof Request){
                Request rf = (Request) msg;
                switch (rf.getCommand()){
                    case ("rename"):
                        if (Files.exists(Paths.get("server_file", nikName, rf.getFilename()))){
                            Path oldname = Paths.get("server_file", nikName, rf.getFilename());
                            Files.move(oldname, oldname.resolveSibling(rf.getNewFilename()));
                            if(Files.exists(Paths.get("server_file",nikName, rf.getNewFilename()))){
                                refreshServerFileList(ctx);
                                ctx.writeAndFlush(new Message("Файл успешно переименован"));
                            } else {
                                ctx.writeAndFlush(new Message("Ошибка переименования"));
                            }
                        } else {
                            ctx.writeAndFlush(new Message("Файл не найден"));
                        }
                        break;
                    case ("download"):
                        if (Files.exists(Paths.get("server_file", nikName, rf.getFilename()))) {
                            FileMessage fms = new FileMessage(Paths.get("server_file", nikName, rf.getFilename()));
                            ctx.writeAndFlush(fms);
                        } else {
                            ctx.writeAndFlush(new Message("Файл не найден"));
                        }
                        break;
                    case ("delete"):
                        if (Files.exists(Paths.get("server_file", nikName, rf.getFilename()))) {
                            Files.delete(Paths.get("server_file", nikName, rf.getFilename()));
                            if(Files.exists(Paths.get("server_file", nikName, rf.getFilename()))){
                                ctx.writeAndFlush(new Message("Ошибка удаления"));
                            } else {
                                refreshServerFileList(ctx);
                                ctx.writeAndFlush(new Message("Файл успешно удален"));
                            }
                        } else {
                            ctx.writeAndFlush(new Message("Файл не найден"));
                        }
                        break;
                    case ("refresh"):
                        refreshServerFileList(ctx);     // отправляем массиы со списом файлов на сервере по запросу
                        break;
                    case ("loginOk"):
                        ctx.writeAndFlush(new Request("loginOk", nikName));
                        refreshServerFileList(ctx);
                        break;
                    case ("logOut"):
                        ctx.writeAndFlush(new Close());
                        ctx.pipeline().removeLast();
                        ctx.pipeline().addLast(new AuthService());
                        break;
                }
            }
            if(msg instanceof Close){
                ctx.writeAndFlush(new Close());
                ctx.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private void refreshServerFileList(ChannelHandlerContext ctx){      //Метод для обновления списка файлов на сервере
        try {
            Files.list(Paths.get("server_file",nikName)).map(p -> p.getFileName().toString()).forEach(o -> fileServerList.add(o));
            ctx.writeAndFlush(new Request("s_refresh", fileServerList));
            fileServerList.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
