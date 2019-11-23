/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.net.*;
import java.util.*;
public class Servidor{

    private ServerSocket ss;
    private int numJugadores;
    private ConexionServidor jugador1;
    private ConexionServidor jugador2;
    private int pozoJugador1;
    private int pozoJugador2;
    private int puntosJugador1;
    private int puntosJugador2;
    private int pozoTotal;

    public Servidor(){
        System.out.println("Servidor del juego 0,1,2,3");
        numJugadores = 0;
        pozoTotal = 0;
        try {
            ss = new ServerSocket(51734);
        } catch (IOException e) {
            System.out.println("Error en el constructor");
        }
    }

    public void aceptarConexion(){
        try {
            System.out.println("Esperando a los jugadores " + numJugadores +"/2.");
            while(numJugadores < 2){
                Socket s = ss.accept();
                numJugadores++;
                System.out.println("El jugador numero " + numJugadores + " se ha conectado.");
                ConexionServidor conexionServidor = new ConexionServidor(s, numJugadores);
                if (numJugadores == 1){
                    puntosJugador1 = 0;
                    jugador1 = conexionServidor;
                }
                else{
                    puntosJugador2 = 0;
                    jugador2 = conexionServidor;
                }
                Thread thread = new Thread(conexionServidor);
                thread.start();
            }
            System.out.println("Servidor en su máxima capacidad.");
        }
        catch(IOException e){
            System.out.println("Error al aceptar conexion");
        }
    }

    private class ConexionServidor implements Runnable{
        private Socket socket;
        private DataInputStream entrada;
        private DataOutputStream salida;
        private int idJugador;
        private String nombreJugador;

        public ConexionServidor(Socket s, int id){
            socket = s;
            idJugador = id;
            try {
                entrada = new DataInputStream(socket.getInputStream());
                nombreJugador = entrada.readUTF();
                System.out.println(nombreJugador);
                salida = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.out.println("Error en el constructor de la Conexion del Servidor.");
            }
        }

        public void run(){
            try {
                salida.writeInt(idJugador);
                Random random = new Random();
                int indiceMano = random.nextInt(6);
                salida.writeInt(indiceMano);
                salida.flush();

                while(true){
                    if(idJugador == 1){
                        pozoJugador1 = entrada.readInt();
                        pozoTotal = pozoJugador1 + pozoTotal;
                        System.out.println("El jugador " + idJugador + " a seleccionado la carta con valor " + pozoJugador1 + ".");
                        if(pozoTotal > 9){
                            puntosJugador2++;
                            salida.writeUTF("<html><center>Perdiste</center><br>Jugador " + nombreJugador + ": " + puntosJugador1 + "<br>"+ "Jugador " + jugador2.nombreJugador + ": " + puntosJugador2 +"</html>");
                            salida.flush();
                            jugador2.enviarPozo(pozoTotal);
                            jugador2.enviarMensaje("<html><center>Ganaste</center><br>Jugador " + nombreJugador + ": " + puntosJugador1 + "<br>"+ "Jugador " + jugador2.nombreJugador + ": " + puntosJugador2 +"</html>");
                            pozoTotal = 0;
                            System.out.println("==== Siguiente ronda ====");
                        }
                        else{
                            jugador2.enviarPozo(pozoTotal);
                        }
                    }
                    else{
                        pozoJugador2 = entrada.readInt();
                        pozoTotal = pozoJugador2 + pozoTotal;
                        System.out.println("El jugador " + idJugador + " a seleccionado la carta con valor " + pozoJugador2 + ".");    
                        if(pozoTotal > 9){
                            puntosJugador1++;
                            salida.writeUTF("<html><center>Perdiste</center><br>Jugador " + jugador1.nombreJugador + ": " + puntosJugador1 + "<br>" + "Jugador " + nombreJugador + ": " + puntosJugador2 + "</html>");
                            salida.flush();
                            jugador1.enviarPozo(pozoTotal);
                            jugador1.enviarMensaje("<html><center>Ganaste</center><br>Jugador " + jugador1.nombreJugador + ": " + puntosJugador1 + "<br>" + "Jugador " + nombreJugador + ": " + puntosJugador2 + "</html>");
                            pozoTotal = 0;
                            System.out.println("==== Siguiente ronda ====");
                        }
                        else{
                            jugador1.enviarPozo(pozoTotal);
                        }
                    }
                }


            } catch (IOException e) {
                System.out.println("Error en el constructor de la Conexion del Servidor.");                
            }
        }

        public void enviarPozo(int pozo){
            try {
                salida.writeInt(pozo);
                salida.flush();
            } catch (IOException e) {
                System.out.println("Error al momento de enviar el pozo.");
            }
        }
        
        public void enviarMensaje(String mensaje){
            try{
                salida.writeUTF(mensaje);
                salida.flush();
            }
            catch(IOException e){
                System.out.println("Error al momento de enviar el mensaje.");
            }
        }
        
    }
    
    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.aceptarConexion();
    }
}