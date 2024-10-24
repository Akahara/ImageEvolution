package fr.wonder.gl;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallbackI;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

public class GLWindow {
	
	private static long window;
	private static int winWidth, winHeight;
	
	private static final List<Callback> closeableCallbacks = new ArrayList<>();

	public static void createWindow(String title, int width, int height) {
		winWidth = width;
		winHeight = height;

		GLFWErrorCallback.createPrint(System.err).set();
		
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW !");

		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
//		glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		
		window = glfwCreateWindow(width, height, title, NULL, NULL);

		if (window == NULL)
			throw new IllegalStateException("Unable to create a window !");

		glfwMakeContextCurrent(window);
		
		GL.createCapabilities();
		
		Callback errorCallback = GLUtil.setupDebugMessageCallback(System.err);
		if(errorCallback != null)
			closeableCallbacks.add(errorCallback);
		
		glViewport(0, 0, width, height);
		glClearColor(0, 0.6f, 0, 1);
		GLUtils.enableBlend(true);
		
		glfwSetWindowSizeCallback(window, (win, w, h) -> {
			glViewport(0, 0, w, h);
			winWidth = w;
			winHeight = h;
		});
		
		glfwSetKeyCallback(window, (win, key, scanCode, action, mods) -> {
			if(action == GLFW_PRESS && key == GLFW_KEY_ESCAPE) {
				glfwSetWindowShouldClose(window, true);
			}
		});
	}
	
	public static void addWindowResizeCallback(BiConsumer<Integer, Integer> resizeCallback) {
		GLFWWindowSizeCallbackI old = glfwSetWindowSizeCallback(window, null);
		glfwSetWindowSizeCallback(window, (win, w, h) -> {
			old.invoke(win, w, h);
			resizeCallback.accept(w, h);
		});
	}

	public static void show() {
		glfwShowWindow(window);
		glfwFocusWindow(window);
	}
	
	public static void enableVSync() {
		glfwSwapInterval(1);
	}
	
	public static long getWindowHandle() {
		return window;
	}
	
	public static int getWinWidth() {
		return winWidth;
	}
	
	public static int getWinHeight() {
		return winHeight;
	}

	public static boolean shouldDispose() {
		return glfwWindowShouldClose(window);
	}
	
	public static void dispose() {
		Callbacks.glfwFreeCallbacks(window);
		glfwSetErrorCallback(null).free();
		for(Callback callback : closeableCallbacks)
			callback.free();
		GL.setCapabilities(null);
		GL.destroy();
		glfwDestroyWindow(window);
		glfwTerminate();
		window = 0;
	}
	
	public static void setWindowTitle(String title) {
		glfwSetWindowTitle(window, title);
	}
	
	public static void resizeWindow(int width, int height) {
		winWidth = width <= 0 ? winWidth : width;
		winHeight = height <= 0 ? winHeight : height;
		glfwSetWindowSize(window, winWidth, winHeight);
	}

	public static void sendFrame() {
		glfwSwapBuffers(window);
		glfwPollEvents();
	}
	
}
