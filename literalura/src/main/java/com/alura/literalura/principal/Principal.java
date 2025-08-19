package com.alura.literalura.principal;

import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private LibroRepository libroRepositorio;
    private AutorRepository autorRepositorio;

    public Principal(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepositorio = libroRepository;
        this.autorRepositorio = autorRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """

                    Elija la opción a través de su número:
                    1 - Buscar libro por título
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma

                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivosPorAnio();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Escriba el nombre del libro que desea buscar:");
        var nombreLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "%20"));
        Datos datosBusqueda = conversor.obtenerDatos(json, Datos.class);

        Optional<DatosLibro> libroBuscado = datosBusqueda.resultados().stream()
                .findFirst();

        if (libroBuscado.isPresent()) {
            DatosLibro datosLibro = libroBuscado.get();
            if (libroRepositorio.findByTituloContains(datosLibro.titulo()).isPresent()) {
                System.out.println("El libro ya está registrado en la base de datos.");
                return;
            }

            Autor autor = buscarOcrearAutor(datosLibro);
            Libro libro = new Libro(datosLibro);
            libro.setAutor(autor);
            libroRepositorio.save(libro);

            System.out.println("----- LIBRO GUARDADO CON ÉXITO -----");
            System.out.println(libro);
            System.out.println("------------------------------------");

        } else {
            System.out.println("Libro no encontrado.");
        }
    }

    private Autor buscarOcrearAutor(DatosLibro datosLibro) {
        DatosAutor datosAutor = datosLibro.autor().get(0);
        return autorRepositorio.findByNombre(datosAutor.nombre())
                .orElseGet(() -> autorRepositorio.save(new Autor(datosAutor)));
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = libroRepositorio.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            System.out.println("----- LIBROS REGISTRADOS -----");
            libros.forEach(System.out::println);
            System.out.println("------------------------------");
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepositorio.findAll();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
        } else {
            System.out.println("----- AUTORES REGISTRADOS -----");
            autores.forEach(System.out::println);
            System.out.println("-------------------------------");
        }
    }

    private void listarAutoresVivosPorAnio() {
        System.out.println("Ingrese el año para buscar autores vivos:");
        var anio = teclado.nextInt();
        teclado.nextLine();
        List<Autor> autoresVivos = autorRepositorio.findAutoresVivosEnAnio(anio);
        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + anio + ".");
        } else {
            System.out.println("----- AUTORES VIVOS EN " + anio + " -----");
            autoresVivos.forEach(System.out::println);
            System.out.println("-------------------------------------");
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("Ingrese el idioma para buscar los libros:");
        var menuIdiomas = """
                es - español
                en - inglés
                fr - francés
                pt - portugués
                """;
        System.out.println(menuIdiomas);
        var idioma = teclado.nextLine();
        List<Libro> librosPorIdioma = libroRepositorio.findByIdioma(idioma);
        if (librosPorIdioma.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma '" + idioma + "'.");
        } else {
            System.out.println("----- LIBROS EN IDIOMA '" + idioma + "' -----");
            librosPorIdioma.forEach(System.out::println);
            System.out.println("-----------------------------------");
        }
    }
}