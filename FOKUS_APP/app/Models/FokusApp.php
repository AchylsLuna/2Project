<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class FokusApp extends Model
{
    use HasFactory;

    protected $table = 'fokus_app';

    protected $fillable = [
        'username', 'fullname', 'password', 'email'
    ];
}
