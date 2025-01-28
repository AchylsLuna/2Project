<?php

namespace App\Http\Controllers;

use App\Models\FokusApp;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class fokusController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        return FokusApp::all();
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Request $request)
    {
        // Validate the request
        $request->validate([
            'fullname' => 'required|string|max:255',
            'username' => 'required|string|max:255|unique:fokus_app',
            'password' => 'required|string|min:8',
            'email' => 'required|string|email|max:255|unique:fokus_app',
        ]);

        // Create new FokusApp record
        $fokusApp = new FokusApp([
            'fullname' => $request->fullname,
            'username' => $request->username,
            'password' => Hash::make($request->password), // Hash the password
            'email' => $request->email,
        ]);

        // Save to the database
        $fokusApp->save();

        // Return a response
        return response()->json([
            'message' => 'User created successfully!',
            'data' => $fokusApp
        ], 201);
    }

    /**
     * Display the specified resource.
     */
    public function show(string $id)
    {
        // Find and return the specific resource
        $fokusApp = FokusApp::find($id);
        if ($fokusApp) {
            return response()->json($fokusApp);
        }
        return response()->json(['message' => 'Resource not found.'], 404);
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Request $request, string $id)
    {
        // Find the resource to update
        $fokusApp = FokusApp::find($id);
        if (!$fokusApp) {
            return response()->json(['message' => 'Resource not found.'], 404);
        }

        // Validate the request
        $request->validate([
            'fullname' => 'sometimes|string|max:255',
            'username' => 'sometimes|string|max:255|unique:fokus_app,username,' . $id,
            'password' => 'sometimes|string|min:8',
            'email' => 'sometimes|string|email|max:255|unique:fokus_app,email,' . $id,
        ]);

        // Update the record
        $fokusApp->fill($request->all());
        if ($request->has('password')) {
            $fokusApp->password = Hash::make($request->password);
        }
        $fokusApp->save();

        return response()->json([
            'message' => 'User updated successfully!',
            'data' => $fokusApp
        ]);
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(string $id)
    {
        // Find and delete the specific resource
        $fokusApp = FokusApp::find($id);
        if (!$fokusApp) {
            return response()->json(['message' => 'Resource not found.'], 404);
        }

        $fokusApp->delete();
        return response()->json(['message' => 'User deleted successfully.']);
    }
}
