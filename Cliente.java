import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Random;
import java.io.*;

public class Cliente extends JFrame{

    private int ancho;
    private int alto;
    private Container contenedor;
    private JPanel panelConexion;
    private JPanel panelJuego;
    private JPanel panelReiniciar;
    private JPanel panelCartas;
    private JLabel etiquetaJuego;
    private JButton cartas[];
    private static String valorMano[][] = { {"0","0","3","3","2"},
                                            {"1","2","2","0","3"},
                                            {"1","1","3","3","0"},
                                            {"1","2","3","1","2"},
                                            {"2","3","2","3","0"},
                                            {"1","2","1","2","3"},
                                            {"1","2","3","3","2"}};
    private int pozo;
    private int indiceMano;
    private int idJugador;
    private int idJugadorContrario;
    private Boolean permiteAccion;
    private Boolean verificaConexion;
    private String nombreJugador;
    private ConexionCliente conexionCliente;



    public Cliente (int ancho,int alto){
        contenedor = getContentPane();
        contenedor.setLayout(new BorderLayout(10,10));
        construirInterfazConexion();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                conexionCliente.cerrarConexion();                
            }
        });
    }
    public void construirInterfazConexion(){
        //Se crean los paneles para la interfaz de conexión de clientes
        JPanel panelIP = new JPanel();
        JPanel panelNombre = new JPanel();
        panelConexion = new JPanel();
        //Inicialización y distribución de los paneles
        panelNombre.setLayout(new BorderLayout(10,10));
        panelIP.setLayout(new BorderLayout(10,10));
        panelConexion.setLayout(new BorderLayout(10,10));
        panelConexion.add(panelIP,BorderLayout.NORTH);
        panelConexion.add(panelNombre,BorderLayout.CENTER);
        //Creación de los componentes
        JLabel nombreJugadorLabel = new JLabel("Ingrese el nombre del jugador",SwingConstants.CENTER);
        JLabel ipServidorLabel = new JLabel("Ingrese la IP del servidor",SwingConstants.CENTER);
        JTextField nombreJugadorTextField = new JTextField(20);
        JTextField ipServidorLTextField = new JTextField(20);
        JButton conectarServidor = new JButton("Conectar al servidor");
        //Se agregan los componentes a sus respectivos paneles
        panelIP.add(ipServidorLabel,BorderLayout.NORTH);
        panelIP.add(ipServidorLTextField,BorderLayout.CENTER);
        panelNombre.add(nombreJugadorLabel,BorderLayout.NORTH);
        panelNombre.add(nombreJugadorTextField,BorderLayout.CENTER);
        panelNombre.add(conectarServidor,BorderLayout.SOUTH);
        //Agregar los paneles creados a el contenedor
        contenedor.add(panelConexion,BorderLayout.NORTH);
        //Hacer la ventana visible para los usuarios
        this.setVisible(true);
        this.setSize(300,200);
        this.setTitle("Primer taller de Sistemas Distribuidos año 2019");
        //Agrega eventos al botón conectarServidor
        conectarServidor.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                String textoIP = ipServidorLTextField.getText();
                String textoNombre = nombreJugadorTextField.getText();
                //Verifica que el campo de nombreJugador no esté vacío
                if(!textoNombre.equals("")){
                    nombreJugador = textoNombre;
                    conectarAlServidor(textoIP,textoNombre);
                    if(verificaConexion){
                        //Construye la interfaz de las cartas
                        panelConexion.setVisible(false);
                        construirInterfazJuego(alto,ancho);
                    }
                    else{
                        ipServidorLabel.setText("No existe servidor con la IP ingresada.");
                    }
                }
                else{
                    nombreJugadorLabel.setText("Falto ingresar el nombre.");
                }
            }
        });
    }

    public void construirInterfazJuego(int alto, int ancho){
        //Inicializar el contendor
        contenedor.removeAll();
        pozo = 0;
        //Se crean los paneles para la interfaz de juego
        JPanel panelEtiqueta = new JPanel();
        panelCartas = new JPanel();
        panelJuego = new JPanel();
        //Inicialización y distribución de paneles
        panelJuego.setLayout(new BorderLayout(10,10));
        panelJuego.add(panelEtiqueta,BorderLayout.CENTER);
        panelJuego.add(panelCartas,BorderLayout.SOUTH);
        panelEtiqueta.setLayout(new BorderLayout(10,10));
        //Se agregan los componenetes a los paneles
        etiquetaJuego = new JLabel("",SwingConstants.CENTER);
        etiquetaJuego.setFont(new Font("Tahoma",Font.BOLD,25));
        etiquetaJuego.setForeground(new Color(76,98,90));
        panelEtiqueta.add(etiquetaJuego);

        //Controla la primera acción de cada jugador
        if(idJugador == 1){
            etiquetaJuego.setText("Jugador " + nombreJugador + ". Usted va primero.");
            idJugadorContrario = 2;
            permiteAccion = true;
        }
        else{
            etiquetaJuego.setText("Jugador " + nombreJugador + ". Espere su turno.");
            idJugadorContrario = 1;
            permiteAccion = false;
            Thread thread = new Thread( new Runnable(){
                @Override
                public void run() {
                    actualizarTurno();
                }
            });
            thread.start();
        }  

        //Establece la interfaz del juego
        establecerCartasPanel(panelCartas);
        establecerAccionCartas();
        gestionarAccion();
        contenedor.add(panelJuego,BorderLayout.CENTER);
        contenedor.revalidate();
        contenedor.repaint();
        this.setSize(500,270);
        this.setVisible(true);
        this.setTitle("Cliente del jugador " + idJugador +": " + nombreJugador);
    }

    public void establecerCartasPanel(JPanel panel){
        panel.removeAll();
        cartas = new JButton[5];
        for(int i = 0; i < 5 ; i++){
            cartas[i] = new JButton(valorMano[indiceMano][i]);
            cartas[i].setPreferredSize(new Dimension(85,135));
            panel.add(cartas[i]);
        }
        panel.setBackground(new Color(123,146,120));
        panel.revalidate();
        panel.repaint();
    }

    public void establecerAccionCartas(){
        ActionListener actionListener = new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton carta = (JButton) e.getSource();
                carta.setVisible(false);
                int valorCarta = Integer.parseInt(carta.getText());                
                pozo += valorCarta;
                permiteAccion = false;
                gestionarAccion();
                conexionCliente.enviarValorCarta(valorCarta);
                if(pozo <= 9){
                    etiquetaJuego.setText(Integer.toString(pozo));
                }
                else{
                    construirPanelReiniciarJuego();
                }
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        actualizarTurno();
                    }
                });
                thread.start();
            }
        };
        for(int i=0; i < 5; i++){
            cartas[i].addActionListener(actionListener);
        }
    }
    
    public void construirPanelReiniciarJuego(){
        //Oculta las cartas
        pozo = 0;
        panelJuego.setVisible(false);
        System.out.println("=========== Siguiente ronda ========== ");

        //Se crean los paneles del reinicio
        JPanel panelEtiqueta = new JPanel();
        JPanel panelBotonReiniciar = new JPanel();
        panelReiniciar = new JPanel();

        //Inicialización y distribución de paneles
        panelReiniciar.setLayout(new BorderLayout(10,10));
        
        //Agrega los componentes en la interfaz
        panelReiniciar.add(panelEtiqueta,BorderLayout.NORTH);
        panelReiniciar.add(panelBotonReiniciar,BorderLayout.SOUTH);

        JLabel resumenPartida = new JLabel("",SwingConstants.CENTER);
        String marcador = conexionCliente.recibirMensaje();
        resumenPartida.setText(marcador);
        resumenPartida.setFont(new Font("Tahoma",Font.BOLD,18));
        resumenPartida.setForeground(new Color(76,98,90));
        panelEtiqueta.add(resumenPartida);
        JButton botonReiniciar = new JButton("Reiniciar");
        botonReiniciar.setPreferredSize(new Dimension(150,100));
        panelBotonReiniciar.add(botonReiniciar);
        panelBotonReiniciar.setBackground(new Color(123,146,120));
        //Establece el evento para reiniciar el juego
        botonReiniciar.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                panelReiniciar.setVisible(false);
                etiquetaJuego.setText("0");
                mostrarCartas();
                panelJuego.setVisible(true);
            }
        });   
        contenedor.add(panelReiniciar,BorderLayout.SOUTH);
        this.setSize(500,270);
        this.setVisible(true);
    }

    public void actualizarTurno(){
        int pozoActual = conexionCliente.recibirPozo();
        etiquetaJuego.setText(Integer.toString(pozoActual));
        if(pozoActual <= 9){
            permiteAccion = true;
            gestionarAccion();
        }
        else{
            permiteAccion = true;
            gestionarAccion();
            construirPanelReiniciarJuego();
        }

    }

    public void mostrarCartas(){
        Random random = new Random();
        int indiceMano = random.nextInt(6);
        for(int i = 0; i < 5; i++){
            cartas[i].setText(valorMano[indiceMano][i]);
            cartas[i].setVisible(true);
        }
    }

    public void gestionarAccion(){
        for(int i = 0; i < 5 ;i++){
            cartas[i].setEnabled(permiteAccion);
        }
    }

    public void conectarAlServidor(String ip, String nombre){
        conexionCliente = new ConexionCliente(ip, nombre);
    }

    private class ConexionCliente{
        private Socket socket;
        private DataInputStream entrada;
        private DataOutputStream salida;

        public ConexionCliente(String ip, String nombre){
            System.out.println("Cliente");
            try{
                socket = new Socket(ip,51734);
                entrada = new DataInputStream(socket.getInputStream());
                salida = new DataOutputStream(socket.getOutputStream());
                salida.writeUTF(nombre);
                salida.flush();
                idJugador = entrada.readInt();
                indiceMano = entrada.readInt();
                System.out.println("Conectandose como el jugador #" + idJugador + ".");
                verificaConexion = true;
            }
            catch(IOException e){
                System.out.println("Error en el constructor de la clase ConexionCliente");
                verificaConexion = false;
            }
        }

        public void enviarValorCarta(int n){
            try{
                salida.writeInt(n);
                salida.flush();
            }
            catch(IOException e){
                System.out.println("Error en el envío de datos entre el cliente al servidor.");
            }
        }

        public int recibirPozo(){
            pozo = 0;
            try {
                pozo = entrada.readInt();
                System.out.println("En el pozo hay "+ pozo);
            } catch (IOException e) {
                System.out.println("Error en la recepción de datos entre el cliente y servidor.");
            }
            return pozo;
        }

        public String recibirMensaje(){
            String mensaje = "";
            try{
                mensaje = entrada.readUTF();
            }
            catch(IOException e){
                System.out.println("Error en la recepción del mensaje de perdí.");
            }
            return mensaje;
        }

        public void cerrarConexion(){
            try{
                socket.close();
                System.out.println("Cerrando conexión de " + nombreJugador);
            }
            catch (IOException e){
                System.out.println("Error al momento de cerrar el socket.");
            }
        }
    }

    public static void main(String[] args) {
        Cliente cliente = new Cliente(300, 500);
    }
}