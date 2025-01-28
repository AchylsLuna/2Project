<?php

use App\Http\Controllers\FokusController;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;

// Define routes for FokusApp
Route::get('/FokusApp', [FokusController::class, 'index']);
Route::post('/FokusApp', [FokusController::class, 'store']);
Route::get('/FokusApp/{id}', [FokusController::class, 'show']);


Route::post('/login', [FokusController::class, 'login']);

// Example user route with middleware
Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');
