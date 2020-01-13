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

    ArrayList<String> fileServerList = new ArrayList<>();
    private String nikName;

    public ServerHandler (String nikName){
        this.nikName = nikName;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("server_file/" + nikName + "/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                if (Files.exists(Paths.get("server_file/" + nikName + "/" + fm.getFilename()))) {
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
                        if (Files.exists(Paths.get("server_file/" + nikName + "/" + rf.getFilename()))){
                            Path oldname = Paths.get("server_file/" + nikName + "/" + rf.getFilename());
                            Files.move(oldname, oldname.resolveSibling(rf.getNewFilename()));
                            if(Files.exists(Paths.get("server_file/" + nikName + "/" + rf.getNewFilename()))){
                                refreshServerFileList(ctx);
                                ctx.writeAndFlush(new Message("Rename File successful."));
                            } else {
                                ctx.writeAndFlush(new Message("Rename file error."));
                            }
                        } else {
                            ctx.writeAndFlush(new Message("Request 'rename': File not found."));
                        }
                        break;
                    case ("download"):
                        if (Files.exists(Paths.get("server_file/" + nikName + "/" + rf.getFilename()))) {
                            FileMessage fms = new FileMessage(Paths.get("server_file/" + nikName + "/" + rf.getFilename()));
                            ctx.writeAndFlush(fms);
                            ctx.writeAndFlush(new Request("c_refresh"));
                        } else {
                            ctx.writeAndFlush(new Message("Request 'send': File not found."));
                        }
                        break;
                    case ("delete"):
                        if (Files.exists(Paths.get("server_file/" + nikName + "/" + rf.getFilename()))) {
                            Files.delete(Paths.get("server_file/" + nikName + "/" + rf.getFilename()));
                            if(Files.exists(Paths.get("server_file/" + nikName + "/" + rf.getFilename()))){
                                ctx.writeAndFlush(new Message("Delete File error."));
                            } else {
                                refreshServerFileList(ctx);
                                ctx.writeAndFlush(new Message("Delete file successful."));
                            }
                        } else {
                            ctx.writeAndFlush(new Message("Request 'delete': File not found."));
                        }
                        break;
                    case ("refresh"):
                        refreshServerFileList(ctx);
                        break;
                    case ("loginOk"):
                        ctx.writeAndFlush(new Request("loginOk", nikName));
                        break;
                    case ("close"):
                        ctx.writeAndFlush(new Request("close"));
                        ctx.close();
                        break;
                }
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

    private void refreshServerFileList(ChannelHandlerContext ctx){
        try {
            Files.list(Paths.get("server_file/" + nikName + "/")).map(p -> p.getFileName().toString()).forEach(o -> fileServerList.add(o));
            ctx.writeAndFlush(new Request("s_refresh", fileServerList));
            fileServerList.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
