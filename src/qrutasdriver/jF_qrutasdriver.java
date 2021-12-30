/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qrutasdriver;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import org.apache.commons.codec.Resources;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author NA002456
 */
public class jF_qrutasdriver extends javax.swing.JFrame{

    private boolean HTTPSconectado; 
    private boolean runLocal;
    private int keepalive;
    private File folder;
    private boolean logging;
    private final String https_servidor;
    private ArrayList<String> al_rutas;
    
    /**
     * Creates new form jF_qrutasdriver
     */
    public jF_qrutasdriver() {        
        runLocal = true;
        initComponents();
        HTTPSconectado = false;
        logging = false;
        https_servidor = runLocal?"127.0.0.1":"10.116.243.234";
        al_rutas = new ArrayList();        
    }
    
    private void set_estado(String f_texto, Color f_color){
        (new Thread(new Runnable() {               
            public void run() {
                jL_status.setForeground(f_color);
                jL_status.setText(f_texto);
            }
        })).start(); 
    }
    
    private void get_ping(){
        (new Thread(new Runnable() {               
            public void run() {
                do {                    
                    try{Thread.sleep(10000);} catch (Exception e){}
                    if(get_https_response("https://" + https_servidor+ "/qrutas/ping.php", "Ping")){
                    } else{
                    }

                } while(true);                  
            }
        })).start(); 
    }
    
    private void crear_layout(){    
        ArrayList<JPanel> listOfRutaPanels = new ArrayList();
        ArrayList<Integer> listOfNumOfLogs = new ArrayList();
        
        folder = new File("logs/");          
        for (File rutadir : folder.listFiles()) {            
            if(rutadir.isDirectory()){
                if(al_rutas.contains(rutadir.getName())){
                    System.out.println(rutadir.getName());
                    int NumOfLogs = rutadir.listFiles().length;                    
                    if(NumOfLogs == 0) continue;                    
                    JPanel panelrutas = new JPanel();
                    panelrutas.setBorder(BorderFactory.createTitledBorder(rutadir.getName()));
                    panelrutas.setLayout(new GridLayout(NumOfLogs, 2, 10, 5));
                    for (File rutalog : rutadir.listFiles()) {
                        String fecha_log = "";
                        try {
                            fecha_log = (new SimpleDateFormat("yyyyMMddhhmmss", 
                                    new Locale("es", "ES")).parse("2020" + rutalog.getName())).toString();
                        } catch (ParseException ex) {
                            System.out.println(ex.getMessage());
                        }
                        JProgressBar f_PB_logs = new JProgressBar(0,100);
                        f_PB_logs.setValue(0);
                        f_PB_logs.setStringPainted(true);
                        JButton btn_log = new JButton(rutalog.getName() + " - " + fecha_log);
                        btn_log.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btn_log.setEnabled(false);
                                (new Thread(new Runnable() {                
                                    public void run() {
                                        if(load_logsxsubir(rutalog.getName(), rutadir.getName())){
                                            System.out.println("Log " + rutalog.getName() + " de la ruta " 
                                                    + rutadir.getName() + " subido con exito");
                                            if(test_logfile(rutalog)){
                                                System.out.println("Log " + rutalog.getName() + " esta listo para subir");
                                                if(subir_logs(rutalog, f_PB_logs)){
                                                    System.out.println("Log subido exitosamente");
                                                    if(renombrar_logfile(rutalog.getName(), rutadir.getName(), System.getProperty("user.name"))){
                                                        System.out.println("Log renombrado exitosamente");
                                                        try {
                                                            Path temp = Files.move(Paths.get("logs/" + rutadir.getName() + "/" + rutalog.getName())
                                                                    ,Paths.get("logs/fin/" + (rutadir.getName().split(" "))[0] + "_" + rutalog.getName()));
                                                            System.out.println("Log movido exitosamente");
                                                            crear_layout();
                                                        } catch (IOException ex) {
                                                            System.out.println("Error moviendo archivo: " + ex.getMessage());
                                                        }
                                                    } else{
                                                        System.out.println("Fallo renombre del log");
                                                        btn_log.setEnabled(true);
                                                    }                                                    
                                                } else{
                                                    System.out.println("Fallo subida del log");
                                                    btn_log.setEnabled(true);
                                                }
                                            } else{
                                                System.out.println("Error testeando log " + rutalog.getName());
                                                btn_log.setEnabled(true);
                                            }                                            
                                        } else {
                                            System.out.println("Error subiendo log " + rutalog.getName());
                                            btn_log.setEnabled(true);
                                        }                                            
                                    }
                                })).start();
                            }
                        });
                        panelrutas.add(btn_log);                        
                        panelrutas.add(f_PB_logs);
                    }
                    listOfRutaPanels.add(panelrutas);
                    listOfNumOfLogs.add(NumOfLogs);
                }                    
            }
        }                     
        
        //--------------------------
        
        JScrollPane f_SP_params = new JScrollPane();
        JPanel f_P_scrollparams = new JPanel();
        f_SP_params.setViewportView(f_P_scrollparams);
        
        jP_logs.removeAll();
        GroupLayout f_P_paramsLayout = new GroupLayout(jP_logs);
               
        f_P_paramsLayout.setHorizontalGroup(f_P_paramsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(f_SP_params, GroupLayout.PREFERRED_SIZE, 900, GroupLayout.PREFERRED_SIZE)
        );
        f_P_paramsLayout.setVerticalGroup(f_P_paramsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(f_SP_params, GroupLayout.PREFERRED_SIZE, 300, GroupLayout.PREFERRED_SIZE)
        );
        jP_logs.setLayout(f_P_paramsLayout); 
        
        GroupLayout f_P_scrollparamsLayout = new GroupLayout(f_P_scrollparams);
        
        GroupLayout.ParallelGroup grupoparalelo = f_P_scrollparamsLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        
        for(JPanel ipanelruta : listOfRutaPanels){
            grupoparalelo.addComponent(ipanelruta, GroupLayout.PREFERRED_SIZE, 890, GroupLayout.PREFERRED_SIZE);
        }
        
        f_P_scrollparamsLayout.setHorizontalGroup(f_P_scrollparamsLayout.createSequentialGroup()
            .addGroup(grupoparalelo));
        
        GroupLayout.SequentialGroup secuencia = f_P_scrollparamsLayout.createSequentialGroup();
        
        for(JPanel ipanelruta : listOfRutaPanels){
            int NumberOfLogs = listOfNumOfLogs.get(listOfRutaPanels.indexOf(ipanelruta));
            secuencia.addComponent(ipanelruta, GroupLayout.PREFERRED_SIZE, 
                    NumberOfLogs * (60 - NumberOfLogs * 6), GroupLayout.PREFERRED_SIZE);
        }
        
        f_P_scrollparamsLayout.setVerticalGroup(secuencia);

        f_P_scrollparams.setLayout(f_P_scrollparamsLayout);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jB_connect = new javax.swing.JButton();
        jB_salir = new javax.swing.JButton();
        jP_logs = new javax.swing.JPanel();
        jL_status = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("La Huella");

        jB_connect.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jB_connect.setText("Conectar");
        jB_connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_connectActionPerformed(evt);
            }
        });

        jB_salir.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jB_salir.setText("Salir");
        jB_salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jB_salirActionPerformed(evt);
            }
        });

        jP_logs.setBackground(new java.awt.Color(255, 255, 102));

        javax.swing.GroupLayout jP_logsLayout = new javax.swing.GroupLayout(jP_logs);
        jP_logs.setLayout(jP_logsLayout);
        jP_logsLayout.setHorizontalGroup(
            jP_logsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );
        jP_logsLayout.setVerticalGroup(
            jP_logsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );

        jL_status.setFont(new java.awt.Font("Tahoma", 1, 36)); // NOI18N
        jL_status.setText("Desconectado");
        jL_status.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jL_statusMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jB_connect, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 644, Short.MAX_VALUE)
                        .addComponent(jB_salir, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jP_logs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jL_status))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jB_salir, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jB_connect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jL_status)
                .addGap(51, 51, 51)
                .addComponent(jP_logs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(334, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jB_connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_connectActionPerformed
        (new Thread(new Runnable() {               
            public void run() {
                jB_connect.setEnabled(false);
                System.out.println("Connecting...");
                set_estado("Conectando...", Color.black);
                if(get_https_response("https://" + https_servidor+ "/qrutas/connect.php", "Conectado")){
                    System.out.println("Connected");                    
                    set_estado("Conectado", Color.blue);
                    jB_connect.setVisible(false);
                    keepalive = 10;
                    HTTPSconectado = true;
                    al_rutas = carga_lista_rutas();   
                    crear_layout();
//                    get_ping();
                } else{
                    set_estado("Error al conectar!", Color.red);
                    jB_connect.setEnabled(true);
                }
            }
        })).start();              
    }//GEN-LAST:event_jB_connectActionPerformed

    private boolean get_https_response(String f_url, String f_response){
        System.out.println("Getting response \"" + f_response + "\" from: " + f_url);
        String https_response = "";
        
        //Creating a HttpClient object        
        CloseableHttpClient httpclient = null;        
        try{
            httpclient = HttpClients
                .custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        } catch(NoSuchAlgorithmException ex) {System.out.println(ex.getMessage());
        } catch(KeyManagementException ex) {System.out.println(ex.getMessage());            
        } catch(KeyStoreException ex) {System.out.println(ex.getMessage());}

        //Creating a HttpGet object
        HttpGet httpget = new HttpGet(f_url);

        //Printing the method used
        System.out.println("Request Type: " + httpget.getMethod());
        
        try {
            //Executing the Get request
            HttpResponse httpresponse = httpclient.execute(httpget);
            Scanner sc = new Scanner(httpresponse.getEntity().getContent());

            //Printing the status line
            System.out.println("Status: " + httpresponse.getStatusLine());
            while(sc.hasNext()) {                
                https_response += sc.nextLine();
            }
        } 
        catch(IOException ex) {System.out.println(ex.getMessage());}
        finally {
            try {
                httpclient.close();
            } catch (IOException ex) {System.out.println(ex.getMessage());}
        }
        System.out.println("Response: " + https_response);
        System.out.println("Finished getting response");
        return https_response.contains(f_response);
    }    
    
    private String get_https_responsejson(String f_url){
        System.out.println("Getting response json from: " + f_url);
        String https_response = "";
        
        //Creating a HttpClient object        
        CloseableHttpClient httpclient = null;        
        try{
            httpclient = HttpClients
                .custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
        } catch(NoSuchAlgorithmException ex) {System.out.println(ex.getMessage());
        } catch(KeyManagementException ex) {System.out.println(ex.getMessage());            
        } catch(KeyStoreException ex) {System.out.println(ex.getMessage());}

        //Creating a HttpGet object
        HttpGet httpget = new HttpGet(f_url);

        //Printing the method used
        System.out.println("Request Type: " + httpget.getMethod());
        
        try {
            //Executing the Get request
            HttpResponse httpresponse = httpclient.execute(httpget);
            Scanner sc = new Scanner(httpresponse.getEntity().getContent());

            //Printing the status line
            System.out.println("Status: " + httpresponse.getStatusLine());
            while(sc.hasNext()) {                
                https_response += sc.nextLine();
            }
        } 
        catch(IOException ex) {System.out.println(ex.getMessage());}
        finally {
            try {
                httpclient.close();
            } catch (IOException ex) {System.out.println(ex.getMessage());}
        }
        System.out.println("Response: " + https_response);
        System.out.println("Finished getting responsejson");
        return https_response.trim();
    }    
    
    private void jB_salirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jB_salirActionPerformed
        System.out.println("Saliendo");
        System.exit(0);
    }//GEN-LAST:event_jB_salirActionPerformed

    private void jL_statusMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jL_statusMouseClicked
        logging = true;
    }//GEN-LAST:event_jL_statusMouseClicked
    
    private ArrayList carga_lista_rutas(){
        ArrayList url_list_al = new ArrayList();
        String respuestajson = get_https_responsejson("https://" + https_servidor + "/qrutas/rutas.php");
        JsonObject jsonObject = new JsonParser().parse(respuestajson).getAsJsonObject();
        for(String alruta : jsonObject.keySet()){
            url_list_al.add(jsonObject.get(alruta).toString().trim().replaceAll("^\"|\"$", ""));
        }
        System.out.println("Rutas:" + url_list_al);
        return url_list_al;
    }
    
    private boolean subir_logs(File f_file, JProgressBar f_pb_log){
        boolean update_response = false;
        try {
            f_pb_log.setValue(0);
            ResponseHandler<String> handler = new BasicResponseHandler();
            CloseableHttpClient client = HttpClients.createDefault();
            
            try{
                client = HttpClients
                    .custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();
            } catch(NoSuchAlgorithmException ex){

            } catch(KeyManagementException ex){

            } catch(KeyStoreException ex){

            }            
            
            HttpPost httpPost = new HttpPost("https://" + https_servidor + "/qrutas/upload.php");

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(
              "upfile", Resources.getInputStream(f_file), ContentType.APPLICATION_OCTET_STREAM, f_file.getName());
            HttpEntity multipart = builder.build();

            ProgressListener pListener = percentage -> {
                f_pb_log.setValue((int)percentage);
//                System.out.println(percentage);
            };
            httpPost.setEntity(new ProgressEntityWrapper(multipart, pListener));

            String response = client.execute(httpPost, handler);
//            System.out.println(response);
            if(response.equals("LogSubido")) update_response = true;
            client.close();
        }
        catch(IOException ex){System.out.println(ex.getMessage());}
        return update_response;
    }
    
    private boolean load_logsxsubir(String f_log, String f_ruta){
        return get_https_response("https://" + https_servidor + "/qrutas/insertlogname.php?log=" + f_log 
                + "&rutaid=" + (f_ruta.split(" "))[0],"LognameInserted");
    }
    
    private boolean test_logfile(File f_file){
        return get_https_response("https://" + https_servidor + "/qrutas/testlogfile.php?log=" + f_file.getName()
                + "&logsize=" + f_file.length(),"ListoParaSubir");
    }
    
    private boolean renombrar_logfile(String f_log, String f_ruta, String f_sysname){
        return get_https_response("https://" + https_servidor + "/qrutas/renamelogfile.php?log=" + f_log 
                + "&rutaid=" + (f_ruta.split(" "))[0] + "&sysname=" + f_sysname,"LognameRenamed");
    }
    
    public static class CountingOutputStream extends FilterOutputStream {
        private ProgressListener listener;
        private long transferred;
        private long totalBytes;

        public CountingOutputStream(
          OutputStream out, ProgressListener listener, long totalBytes) {
            super(out);
            this.listener = listener;
            transferred = 0;
            this.totalBytes = totalBytes;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            transferred += len;
            listener.progress(getCurrentProgress());
//            System.out.println(getCurrentProgress());
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            transferred++;
            listener.progress(getCurrentProgress());
//            System.out.println(getCurrentProgress());
        }

        private float getCurrentProgress() {
            return ((float) transferred / totalBytes) * 100;
        }
    }
        
    public class ProgressEntityWrapper extends HttpEntityWrapper {
        public ProgressListener listener;

        public ProgressEntityWrapper(HttpEntity entity, ProgressListener listener) {
            super(entity);
            this.listener = listener;
        }

        @Override
        public void writeTo(OutputStream outstream) throws IOException {
            super.writeTo(new CountingOutputStream(outstream, listener, getContentLength()));
        }
    }
    
    public static interface ProgressListener {
        void progress(float percentage);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jB_connect;
    private javax.swing.JButton jB_salir;
    private javax.swing.JLabel jL_status;
    private javax.swing.JPanel jP_logs;
    // End of variables declaration//GEN-END:variables

}
